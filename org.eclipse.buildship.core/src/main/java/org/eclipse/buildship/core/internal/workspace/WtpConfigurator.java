/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

public class WtpConfigurator implements ProjectConfigurator {

    private DefaultGradleBuild gradleBuild;
    private Map<File, EclipseProject> locationToProject;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        // TODO (donat) add required model declarations to the project configurator extension point
        GradleBuild gradleBuild = context.getGradleBuild();
        this.gradleBuild = (DefaultGradleBuild) gradleBuild;
        Collection<EclipseProject> eclipseProjects = ModelProviderUtil.fetchAllEclipseProjects(this.gradleBuild, GradleConnector.newCancellationTokenSource(), FetchStrategy.LOAD_IF_NOT_CACHED, monitor);
        this.locationToProject = eclipseProjects.stream().collect(Collectors.toMap(p -> p.getProjectDirectory(), p -> p));
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        try {
            IProject project = context.getProject();
            EclipseProject model = lookupEclipseModel(project);
            WtpClasspathUpdater.update(JavaCore.create(project), model, this.gradleBuild, monitor);
        } catch (CoreException e) {
            GradleErrorMarker.create(context.getProject(), this.gradleBuild, "Failed to configure project", e, 0);
            CorePlugin.logger().warn("Failed to configure project " + context.getProject().getName(), e);
        }
    }

    private EclipseProject lookupEclipseModel(IProject project) {
        // TODO (donat) duplicate
        IPath path = project.getLocation();
        if (path == null) {
            return null;
        }
        return this.locationToProject.get(path.toFile());
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
    }

}
