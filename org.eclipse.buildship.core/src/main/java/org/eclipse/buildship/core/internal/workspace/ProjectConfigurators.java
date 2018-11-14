/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

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
import org.eclipse.buildship.core.internal.util.gradle.Pair;

public final class ProjectConfigurators {

    private final InternalGradleBuild gradleBuild;
    private final List<InternalProjectConfigurator> contributions;


    private ProjectConfigurators(InternalGradleBuild gradleBuild, List<InternalProjectConfigurator> contributions) {
        this.gradleBuild = gradleBuild;
        this.contributions = contributions;
    }

    List<SynchronizationFailure> initConfigurators(IProgressMonitor monitor) {
        List<SynchronizationFailure> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultInitializationContext context = newInitializationContext(this.gradleBuild);
            try {
                contribution.init(context, progress.newChild(1));
                context.getExceptions().forEach(e -> result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), markerLocation(), configuratorFailedMessage(contribution, e, "initialize"), e));
            }
        }

        return result;
    }

    List<SynchronizationFailure> configureConfigurators(IProject project, IProgressMonitor monitor) {
        List<SynchronizationFailure> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultProjectContext context = newProjectContext(project);
            try {
                contribution.configure(context, progress.newChild(1));
                context.getExceptions().forEach(e -> result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), project, configuratorFailedMessage(contribution, e, "configure project '" + project.getName() + "'"), e));
            }
        }

        return result;
    }

    List<SynchronizationFailure> unconfigureConfigurators(IProject project, IProgressMonitor monitor) {
        List<SynchronizationFailure> result = new ArrayList<>();

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            DefaultProjectContext context = newProjectContext(project);
            try {
                contribution.unconfigure(context, progress.newChild(1));
                context.getExceptions().forEach(e -> result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), markerLocation(), e.getFirst(), e.getSecond())));
            } catch (Exception e) {
                result.add(SynchronizationFailure.from(contribution.getContributorPluginId(), markerLocation(), configuratorFailedMessage(contribution, e, "unconfigure project '" + project.getName() + "'"), e));
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
        return String.format("Project configurator '%s' failed to %s", contribution.getFullyQualifiedId(), operationName);
    }

    private IResource markerLocation() {
        Optional<IProject> projectOrNull = CorePlugin.workspaceOperations().findProjectByLocation(this.gradleBuild.getBuildConfig().getRootProjectDirectory());
        return projectOrNull.isPresent() ? projectOrNull.get() : ResourcesPlugin.getWorkspace().getRoot();
    }


    private static class BaseContext {
        protected final List<Pair<String, Exception>> exceptions = new ArrayList<>();

        public List<Pair<String, Exception>> getExceptions() {
            return this.exceptions;
        }

        public void onError(String message, Exception exception) {
            this.exceptions.add(new Pair<>(message, exception));
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
