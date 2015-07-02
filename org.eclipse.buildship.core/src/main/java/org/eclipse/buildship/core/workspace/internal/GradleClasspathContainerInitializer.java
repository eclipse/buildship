/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;

/**
 * Initializes the classpath of each Eclipse workspace project that has a Gradle nature with the
 * source/project/external dependencies of the underlying Gradle project.
 * <p/>
 * When this initializer is invoked, it looks up the {@link OmniEclipseProject} for the given
 * Eclipse workspace project, takes all the found sources, project dependencies and external
 * dependencies, and assigns them to the {@link ClasspathDefinition#GRADLE_CLASSPATH_CONTAINER_ID}
 * classpath container.
 * <p/>
 * This initializer is assigned to the projects via the
 * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension point.
 * <p/>
 * The initialization is scheduled as a job, to not block the IDE upon startup.
 */
public final class GradleClasspathContainerInitializer extends ClasspathContainerInitializer {

    /**
     * Looks up the {@link OmniEclipseProject} for the target project, takes all external Jar
     * dependencies and assigns them to the classpath container with id
     * {@link ClasspathDefinition#GRADLE_CLASSPATH_CONTAINER_ID}.
     */
    @Override
    public void initialize(IPath containerPath, IJavaProject javaProject) {
        scheduleClasspathInitialization(containerPath, javaProject, FetchStrategy.LOAD_IF_NOT_CACHED);
    }

    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) {
        scheduleClasspathInitialization(containerPath, project, FetchStrategy.FORCE_RELOAD);
    }

    private void scheduleClasspathInitialization(final IPath containerPath, final IJavaProject javaProject, final FetchStrategy fetchStrategy) {
        new ToolingApiWorkspaceJob("Initialize Gradle classpath for project '" + javaProject.getElementName() + "'") {

            @Override
            protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
                monitor.beginTask("Initializing classpath", 100);

                // use the same rule as the ProjectImportJob to do the initialization
                IJobManager manager = Job.getJobManager();
                IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                manager.beginRule(workspaceRoot, monitor);
                try {
                    internalInitialize(containerPath, javaProject, fetchStrategy, monitor);
                } finally {
                    manager.endRule(workspaceRoot);
                }
            }
        }.schedule();
    }

    private void internalInitialize(IPath containerPath, IJavaProject project, FetchStrategy fetchStrategy, IProgressMonitor monitor) throws JavaModelException {
        Optional<OmniEclipseProject> eclipseProject = findEclipseProject(project.getProject(), fetchStrategy);
        if (eclipseProject.isPresent()) {
            // update source folders
            SourceFolderUpdater.update(project, eclipseProject.get().getSourceDirectories());

            // update project/external dependencies
            ClasspathContainerUpdater.update(project, eclipseProject.get(), new org.eclipse.core.runtime.Path(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID));
        } else {
            throw new GradlePluginsRuntimeException(String.format("Cannot find Eclipse project model for project %s.", project.getProject()));
        }
    }

    private Optional<OmniEclipseProject> findEclipseProject(IProject project, FetchStrategy fetchStrategy) {
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(configuration.getRequestAttributes(), fetchStrategy);
        return eclipseGradleBuild.getRootEclipseProject().tryFind(Specs.eclipseProjectMatchesProjectPath(configuration.getProjectPath()));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes, FetchStrategy fetchStrategy) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> noProgressListeners = ImmutableList.of();
        List<org.gradle.tooling.events.ProgressListener> noTypedProgressListeners = ImmutableList.of();
        CancellationToken cancellationToken = GradleConnector.newCancellationTokenSource().token();
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, noProgressListeners,
                noTypedProgressListeners, cancellationToken);
        ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, fetchStrategy);
    }

    /**
     * {@code IClasspathContainer} to describe the external dependencies.
     */
    static final class GradleClasspathContainer implements IClasspathContainer {

        private final String containerName;
        private final IPath path;
        private final IClasspathEntry[] classpathEntries;

        GradleClasspathContainer(String containerName, IPath path, List<IClasspathEntry> classpathEntries) {
            this.containerName = Preconditions.checkNotNull(containerName);
            this.path = Preconditions.checkNotNull(path);
            this.classpathEntries = Iterables.toArray(classpathEntries, IClasspathEntry.class);
        }

        @Override
        public String getDescription() {
            return this.containerName;
        }

        @Override
        public IPath getPath() {
            return this.path;
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return this.classpathEntries;
        }

        @Override
        public int getKind() {
            return IClasspathContainer.K_APPLICATION;
        }
    }

}
