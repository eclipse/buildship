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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;

public class BaseConfigurator implements ProjectConfigurator {

    // TODO (donat) shall we share the original EclipseProject instances via ProjectContext?
    private Map<File, EclipseProject> locationToProject;
    private org.eclipse.buildship.core.internal.workspace.GradleBuild internalGradleBuild;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        GradleBuild gradleBuild = context.getGradleBuild();
        this.internalGradleBuild = ((org.eclipse.buildship.core.internal.DefaultGradleBuild)gradleBuild).getInternalGradleBuild();
        Collection<EclipseProject> eclipseProjects = ModelProviderUtil.fetchAllEclipseProjects(this.internalGradleBuild, GradleConnector.newCancellationTokenSource(), FetchStrategy.LOAD_IF_NOT_CACHED, monitor);
        this.locationToProject = eclipseProjects.stream().collect(Collectors.toMap(p -> p.getProjectDirectory(), p -> p));
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        // TODO (donat) do actual configuration
        EclipseProject model = lookupEclipseModel(context.getProject());
        System.err.println(model.getProjectDirectory());
    }

    private EclipseProject lookupEclipseModel(IProject project) {
        IPath path = project.getLocation();
        if (path == null) {
            return null;
        }
        return this.locationToProject.get(path.toFile());
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
     // TODO (donat) implement
    }

}
