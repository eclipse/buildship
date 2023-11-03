/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

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
                context.getErrors().forEach(e -> result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
                context.getWarnings().forEach(e -> result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation(), configuratorFailedMessage(contribution, e, "initialize"), e));
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
                context.getErrors().forEach(e -> result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
                context.getWarnings().forEach(e -> result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), project, configuratorFailedMessage(contribution, e, "configure project '" + project.getName() + "'"), e));
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
                context.getErrors().forEach(e -> result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
                context.getWarnings().forEach(e -> result.add(SynchronizationProblem.newWarning(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationProblem.newError(contribution.getContributorPluginId(), markerLocation(), configuratorFailedMessage(contribution, e, "unconfigure project '" + project.getName() + "'"), e));
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


    private IResource markerLocation() {
        Optional<IProject> maybeProject = CorePlugin.workspaceOperations().findProjectByLocation(this.gradleBuild.getBuildConfig().getRootProjectDirectory());
        if (!maybeProject.isPresent()) {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
        IProject project = maybeProject.get();
        PersistentModel persistentModel = CorePlugin.modelPersistence().loadModel(project);
        if (!persistentModel.isPresent()) {
            return project;
        }
        IFile buildScript = project.getFile(persistentModel.getbuildScriptPath());
        if (!buildScript.exists()) {
            return project;
        }

        return buildScript;
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
}
