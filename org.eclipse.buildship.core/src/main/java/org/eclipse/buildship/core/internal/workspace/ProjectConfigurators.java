/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.List;

import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.extension.InternalProjectConfigurator;
import org.eclipse.buildship.core.internal.extension.ProjectConfiguratorContribution;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

final class ProjectConfigurators {

    private final GradleBuild gradleBuild;
    private final List<InternalProjectConfigurator> contributions;

    private ProjectConfigurators(GradleBuild gradleBuild, List<InternalProjectConfigurator> contributions) {
        this.gradleBuild = gradleBuild;
        this.contributions = contributions;
    }

    void initConfigurators(IProgressMonitor monitor) {
        InitializationContext context = newInitializationContext(this.gradleBuild);

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            try {
                contribution.init(context, progress.newChild(1));
            } catch (Exception e) {
                logFailureAndAddErrorMarker(contribution, e, "initialize");
            }
        }
    }

    void configureConfigurators(IProject project, IProgressMonitor monitor) {
        ProjectContext context = newProjectContext(project);

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            try {
                contribution.configure(context, progress.newChild(1));
            } catch (Exception e) {
                logFailureAndAddErrorMarker(contribution, e, "configure project '" + project.getName() + "'");
            }
        }
    }

    void unconfigureConfigurators(IProject project, IProgressMonitor monitor) {
        ProjectContext context = newProjectContext(project);

        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(this.contributions.size());
        for (InternalProjectConfigurator contribution : this.contributions) {
            try {
                contribution.unconfigure(context, progress.newChild(1));
            } catch (Exception e) {
                logFailureAndAddErrorMarker(contribution, e, "unconfigure project '" + project.getName() + "'");
            }
        }
    }

    static ProjectConfigurators create(GradleBuild gradleBuild, List<ProjectConfiguratorContribution> configurators) {
        return new ProjectConfigurators(gradleBuild, InternalProjectConfigurator.from(configurators));
    }

    private static InitializationContext newInitializationContext(GradleBuild internalGradleBuild) {
        org.eclipse.buildship.core.GradleBuild gradleBuild = ((DefaultGradleBuild)internalGradleBuild).toApiGradleBuild();
        return new InitializationContext() {

            @Override
            public org.eclipse.buildship.core.GradleBuild getGradleBuild() {
                return gradleBuild;
            }
        };
    }

    private static ProjectContext newProjectContext(IProject project) {
        return new ProjectContext() {

            @Override
            public IProject getProject() {
                return project;
            }
        };
    }

    private void logFailureAndAddErrorMarker(InternalProjectConfigurator contribution, Exception exception, String operationName) {
        String message = String.format("Project configurator '%s' failed to %s", contribution.getFullyQualifiedId(), operationName);
        Status status = new Status(IStatus.WARNING, contribution.getContributorPluginId(), message, exception);
        CorePlugin.getInstance().getLog().log(status);

        IResource targetLocation = markerLocation();
        GradleErrorMarker.create(targetLocation, this.gradleBuild, message, exception, 0);
    }

    private IResource markerLocation() {
        Optional<IProject> projectOrNull = CorePlugin.workspaceOperations().findProjectByLocation(this.gradleBuild.getBuildConfig().getRootProjectDirectory());
        return projectOrNull.isPresent() ? projectOrNull.get() : ResourcesPlugin.getWorkspace().getRoot();
    }
}
