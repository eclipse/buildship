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

package org.eclipse.buildship.core.workspace;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.internal.EclipseGradleBuildModelReloader;

/**
 * Finds the root projects for the selection and issues a classpath update on each related workspace
 * project.
 */
public final class RefreshGradleProjectsJob extends ToolingApiWorkspaceJob {

    private final List<IProject> projects;

    public RefreshGradleProjectsJob(List<IProject> projects) {
        super("Refresh classpath", true);
        this.projects = ImmutableList.copyOf(projects);
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Refresh selected Gradle projects", 2);
        try {
            // find the root projects related to the selection and reload their model
            ImmutableSet<OmniEclipseGradleBuild> gradleBuilds = EclipseGradleBuildModelReloader.from(this.projects, getToken()).reloadRootEclipseModels();
            monitor.worked(1);
            updateAllProjects(gradleBuilds, countProjects(gradleBuilds), new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private int countProjects(ImmutableSet<OmniEclipseGradleBuild> gradleBuilds) {
        int result = 0;
        for (OmniEclipseGradleBuild gradleBuild : gradleBuilds) {
            result += gradleBuild.getRootProject().getAll().size();
        }
        return result;
    }

    private void updateAllProjects(ImmutableSet<OmniEclipseGradleBuild> gradleBuilds, int numberOfAllProjects, IProgressMonitor monitor) {
        monitor.beginTask("Refresh projects", numberOfAllProjects);
        try {
            for (OmniEclipseGradleBuild gradleBuild : gradleBuilds) {
                updateProjectsRecursively(gradleBuild.getRootEclipseProject(), monitor);
            }
        } finally {
            monitor.done();
        }
    }

    private void updateProjectsRecursively(OmniEclipseProject project, IProgressMonitor monitor) {
        monitor.subTask(project.getName());
        update(project);
        monitor.worked(1);
        for (OmniEclipseProject child : project.getChildren()) {
            updateProjectsRecursively(child, monitor);
        }
    }

    private void update(OmniEclipseProject modelProject) {
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByLocation(modelProject.getProjectDirectory());
        if (workspaceProject.isPresent()) {
            update(modelProject, workspaceProject.get());
        }
    }

    private void update(OmniEclipseProject modelProject, IProject workspaceProject) {
        try {
            if (workspaceProject.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject workspacejavaProject = JavaCore.create(workspaceProject);
                GradleClasspathContainer.requestUpdateOf(workspacejavaProject);
            }
            // TODO (donat) the update mechanism should be extended to non-java projects too
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}
