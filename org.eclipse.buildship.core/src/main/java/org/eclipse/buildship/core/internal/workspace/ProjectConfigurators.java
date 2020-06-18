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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.UnresolvedDependencyException;
import org.eclipse.buildship.core.internal.extension.InternalProjectConfigurator;
import org.eclipse.buildship.core.internal.extension.ProjectConfiguratorContribution;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.util.gradle.Pair;

public final class ProjectConfigurators {

    private final InternalGradleBuild gradleBuild;
    private final List<InternalProjectConfigurator> contributions;

    private ProjectConfigurators(InternalGradleBuild gradleBuild, List<InternalProjectConfigurator> contributions) {
        this.gradleBuild = gradleBuild;
        this.contributions = contributions;
    }

    List<SynchronizationProblem> initConfigurators(IProgressMonitor monitor) {
        List<SynchronizationProblem> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultInitializationContext context = newInitializationContext(this.gradleBuild);
            try {
                contribution.init(context, progress.newChild(1));
                context.getErrors().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
                context.getWarnings().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
            } catch (Exception e) {
                MarkerLocation markerLocation = markerLocation(e);
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), configuratorFailedMessage(contribution, e, "initialize"), e, markerLocation.getLine()));
            }
        }

        return result;
    }

    List<SynchronizationProblem> configureConfigurators(IProject project, IProgressMonitor monitor) {
        List<SynchronizationProblem> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultProjectContext context = newProjectContext(project);
            try {
                contribution.configure(context, progress.newChild(1));
                context.getErrors().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
                context.getWarnings().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
            } catch (Exception e) {
                MarkerLocation markerLocation = markerLocation(e);
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), configuratorFailedMessage(contribution, e, "initialize"), e, markerLocation.getLine()));
            }
        }

        return result;
    }

    List<SynchronizationProblem> unconfigureConfigurators(IProject project, IProgressMonitor monitor) {
        List<SynchronizationProblem> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultProjectContext context = newProjectContext(project);
            try {
                contribution.unconfigure(context, progress.newChild(1));

                // TODO use line number if possible here
                context.getErrors().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
                context.getWarnings().forEach(e -> {
                    MarkerLocation markerLocation = markerLocation(e.getSecond());
                    result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation.getResource(), e.getFirst(), e.getSecond(), markerLocation.getLine()));
                });
            } catch (Exception e) {
                MarkerLocation markerLocation = markerLocation(e);
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation.getResource(), configuratorFailedMessage(contribution, e, "initialize"), e, markerLocation.getLine()));
            }
        }

        return result;
    }

    public static ProjectConfigurators create(InternalGradleBuild gradleBuild, List<ProjectConfiguratorContribution> configurators) {
        return new ProjectConfigurators(gradleBuild, InternalProjectConfigurator.from(configurators));
    }

    private static DefaultInitializationContext newInitializationContext(InternalGradleBuild gradleBuild) {
        return new DefaultInitializationContext(gradleBuild);
    }

    private static DefaultProjectContext newProjectContext(IProject project) {
        return new DefaultProjectContext(project);
    }

    private String configuratorFailedMessage(InternalProjectConfigurator contribution, Exception exception, String operationName) {
        return String.format("Project configurator '%s' failed to %s", contribution.getId(), operationName);
    }

    private MarkerLocation markerLocation(Exception exception) {
        Optional<IProject> maybeProject = CorePlugin.workspaceOperations().findProjectByLocation(this.gradleBuild.getBuildConfig().getRootProjectDirectory());
        if (!maybeProject.isPresent()) {
            return new MarkerLocation(ResourcesPlugin.getWorkspace().getRoot(), 0);
        }
        IProject project = maybeProject.get();
        try {
            PersistentModel persistentModel = CorePlugin.modelPersistence().loadModel(project);
            IFile buildScript = project.getFile(persistentModel.getbuildScriptPath());
            if (!buildScript.exists()) {
                return new MarkerLocation(project, 0);
            }

            int lineNumber = 0;
            if (exception instanceof UnresolvedDependencyException) {
                String coordinates = ((UnresolvedDependencyException)exception).getCoordinates();
                InputStream is = buildScript.getContents(true);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    int num = 0;
                    while((line = reader.readLine()) != null) {
                        num++;
                        if (line.contains(coordinates)) {
                            lineNumber = num;
                            break;
                        }
                    }
                }
            }
            return new MarkerLocation(project.getFile(persistentModel.getbuildScriptPath()), lineNumber);
        } catch (Exception ignore) {
            return new MarkerLocation(project, 0);
        }
    }

    private static class BaseContext {
        protected final List<Pair<String, Exception>> errors = new ArrayList<>();
        protected final List<Pair<String, Exception>> warnings = new ArrayList<>();


        public List<Pair<String, Exception>> getErrors() {
            return this.errors;
        }

        public List<Pair<String, Exception>> getWarnings() {
            return this.warnings;
        }

        public void error(String message, Exception exception) {
            this.errors.add(new Pair<>(message, exception));
        }

        public void warning(String message, Exception exception) {
            this.warnings.add(new Pair<>(message, exception));
        }
    }

    private static class DefaultInitializationContext extends BaseContext implements InitializationContext {

        private final InternalGradleBuild gradleBuild;

        DefaultInitializationContext(InternalGradleBuild gradleBuild) {
            this.gradleBuild = gradleBuild;
        }

        @Override
        public GradleBuild getGradleBuild() {
            return this.gradleBuild;
        }
    }

    private static class DefaultProjectContext extends BaseContext implements ProjectContext {

        private final IProject project;

        DefaultProjectContext(IProject project) {
            this.project = project;
        }

        @Override
        public IProject getProject() {
            return this.project;
        }
    }

    private static class MarkerLocation {
        private final IResource resource;
        private final int line;

        public MarkerLocation(IResource resource, int line) {
            this.resource = resource;
            this.line = line;
        }

        public IResource getResource() {
            return this.resource;
        }

        public int getLine() {
            return this.line;
        }
    }
}
