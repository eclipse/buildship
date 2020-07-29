/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionFailureException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.test.Destination;
import org.gradle.tooling.events.test.TestOutputEvent;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseRuntime;
import org.gradle.tooling.model.eclipse.EclipseWorkspaceProject;
import org.gradle.tooling.model.eclipse.RunClosedProjectBuildDependencies;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.IdeFriendlyClassLoading;
import org.eclipse.buildship.core.internal.util.gradle.SimpleIntermediateResultHandler;

public final class EclipseModelUtils {

    private static final String EXCEPTION_DUPLICATE_ROOT_ELEMENT_TEXT = "Duplicate root element ";

    private EclipseModelUtils() {
    }

    public static Collection<EclipseProject> queryModels(ProjectConnection connection) {
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        if (gradleVersion.supportsSendingReservedProjects()) {
            return queryCompositeModelWithRuntimInfo(connection, gradleVersion);
        } else if (gradleVersion.supportsCompositeBuilds()) {
            return queryCompositeModel(EclipseProject.class, connection);
        } else {
            return ImmutableList.of(queryModel(EclipseProject.class, connection));
        }
    }

    public static Collection<EclipseProject> runTasksAndQueryModels(ProjectConnection connection) {
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        if (gradleVersion.supportsSendingReservedProjects()) {
            return runTasksAndQueryCompositeModelWithRuntimInfo(connection, gradleVersion);
        } else if (gradleVersion.supportsSyncTasksInEclipsePluginConfig()) {
            return runTasksAndQueryCompositeModel(connection, gradleVersion);
        } else if (gradleVersion.supportsCompositeBuilds()) {
            return queryCompositeModel(EclipseProject.class, connection);
        } else {
            return ImmutableList.of(queryModel(EclipseProject.class, connection));
        }
    }

    public static EclipseRuntimeConfigurer buildEclipseRuntimeConfigurer() {
        ImmutableList<IProject> allWorkspaceProjects = CorePlugin.workspaceOperations().getAllProjects();
        List<EclipseWorkspaceProject> projects = allWorkspaceProjects.stream().map(p -> new DefaultEclipseWorkspaceProject(p.getName(), p.getLocation().toFile(), p.isOpen()))
                .collect(Collectors.toList());
        return new EclipseRuntimeConfigurer(new DefaultEclipseWorkspace(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), projects));
    }


    private static Collection<EclipseProject> runTasksAndQueryCompositeModelWithRuntimInfo(ProjectConnection connection, GradleVersion gradleVersion) {
        EclipseRuntimeConfigurer buildEclipseRuntimeConfigurer = buildEclipseRuntimeConfigurer();
        try {
            BuildAction<Void> runSyncTasksAction = IdeFriendlyClassLoading.loadClass(TellGradleToRunSynchronizationTasks.class);
            if (gradleVersion.supportsClosedProjectDependencySubstitution()) {
                // use a composite query to run substitute tasks in included builds too
                BuildAction<?> runClosedProjectTasksAction = new CompositeModelQuery<>(RunClosedProjectBuildDependencies.class, EclipseRuntime.class, buildEclipseRuntimeConfigurer);
                BuildActionSequence projectsLoadedAction = new BuildActionSequence(runSyncTasksAction, runClosedProjectTasksAction);
                return runPhasedModelQuery(connection, gradleVersion, projectsLoadedAction, IdeFriendlyClassLoading
                        .loadCompositeModelQuery(EclipseProject.class, EclipseRuntime.class,buildEclipseRuntimeConfigurer));
            }
            return runPhasedModelQuery(connection, gradleVersion, runSyncTasksAction, IdeFriendlyClassLoading
                    .loadCompositeModelQuery(EclipseProject.class, EclipseRuntime.class, buildEclipseRuntimeConfigurer));
        } catch (BuildActionFailureException e) {
            // For gradle >= 5.5 project name deduplication happens in gradle. In case gradle can't deduplicate then create an UnsupportedConfigurationException
            // to match the behaviour with previous gradle versions.
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException && cause.getMessage().startsWith(EXCEPTION_DUPLICATE_ROOT_ELEMENT_TEXT)) {
                String projectName = cause.getMessage().substring(EXCEPTION_DUPLICATE_ROOT_ELEMENT_TEXT.length());
                String message = String.format("A project with the name %s already exists.", projectName);
                throw new UnsupportedConfigurationException(message, e);
            }
            throw e;
        }
    }

    private static Collection<EclipseProject> runTasksAndQueryCompositeModel(ProjectConnection connection, GradleVersion gradleVersion) {
        return runPhasedModelQuery(connection, gradleVersion, IdeFriendlyClassLoading.loadClass(TellGradleToRunSynchronizationTasks.class), IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class));
    }

    private static Collection<EclipseProject> runPhasedModelQuery(ProjectConnection connection, GradleVersion gradleVersion,
            BuildAction<Void> projectsLoadedAction, BuildAction<Collection<EclipseProject>> query) {
        SimpleIntermediateResultHandler<Collection<EclipseProject>> resultHandler = new SimpleIntermediateResultHandler<>();
        connection.action().projectsLoaded(projectsLoadedAction, new SimpleIntermediateResultHandler<Void>()).buildFinished(query, resultHandler).build().forTasks().addProgressListener(new TestOutputForwardingListener()).run();
        return resultHandler.getValue();
    }

    private static Collection<EclipseProject> queryCompositeModelWithRuntimInfo(ProjectConnection connection, GradleVersion gradleVersion) {
        BuildAction<Collection<EclipseProject>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class, EclipseRuntime.class, buildEclipseRuntimeConfigurer());
        return connection.action(query).addProgressListener(new TestOutputForwardingListener()).run();
    }

    private static <T> Collection<T> queryCompositeModel(Class<T> model, ProjectConnection connection) {
        BuildAction<Collection<T>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(model);
        return connection.action(query).addProgressListener(new TestOutputForwardingListener()).run();
    }

    private static <T> T queryModel(Class<T> model, ProjectConnection connection) {
        return connection.model(model).addProgressListener(new TestOutputForwardingListener()).get();
    }

    private static class TestOutputForwardingListener implements ProgressListener {

        private ProcessStreams processStreams;

        public TestOutputForwardingListener() {
            this.processStreams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        }

        @Override
        public void statusChanged(ProgressEvent event) {
            if (event instanceof TestOutputEvent) {
                try {
                    printToConsole((TestOutputEvent) event);
                } catch (IOException e) {
                    CorePlugin.logger().warn("Cannot print test output to console", e);
                }
            }
        }

        private void printToConsole(TestOutputEvent testOutputEvent) throws IOException {
            String message = testOutputEvent.getDescriptor().getMessage();
            Destination destination = testOutputEvent.getDescriptor().getDestination();
            switch (destination) {
                case StdOut:
                    this.processStreams.getOutput().write(message.getBytes());
                    break;
                case StdErr:
                    this.processStreams.getError().write(message.getBytes());
                    break;
                default:
                    throw new IllegalStateException("Invalid destination: " + destination);
            }
        }
    }
}
