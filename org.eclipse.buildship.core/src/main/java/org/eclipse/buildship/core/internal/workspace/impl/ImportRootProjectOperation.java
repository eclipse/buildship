/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.core.internal.workspace.WorkspaceOperations;

/**
 * Imports the root project into the workspace.
 *
 * @author Donat Csikos
 */
public final class ImportRootProjectOperation {

    private final BuildConfiguration buildConfiguration;
    private final NewProjectHandler newProjectHandler;

    public ImportRootProjectOperation(BuildConfiguration buildConfiguration, NewProjectHandler newProjectHandler) {
        this.buildConfiguration = Preconditions.checkNotNull(buildConfiguration);
        this.newProjectHandler = Preconditions.checkNotNull(newProjectHandler);
    }

    public void run(IProgressMonitor monitor) {
        try {
            runInWorkspace(monitor);
        } catch (Exception e) {
            throw new ImportRootProjectException(e);
        }
    }

    private void runInWorkspace(IProgressMonitor monitor) throws CoreException {
        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                SubMonitor progress = SubMonitor.convert(monitor);
                progress.setTaskName("Importing root project");
                progress.setWorkRemaining(3);

                File rootDir = ImportRootProjectOperation.this.buildConfiguration.getRootProjectDirectory();
                verifyNoWorkspaceRootIsImported(rootDir, progress.newChild(1));
                saveProjectConfiguration(ImportRootProjectOperation.this.buildConfiguration, rootDir, progress.newChild(1));
                importRootProject(rootDir, progress.newChild(1));
            }
        }, monitor);
    }

    private void verifyNoWorkspaceRootIsImported(File rootDir, IProgressMonitor monitor) {
        File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        if (rootDir.equals(workspaceRoot)) {
            throw new UnsupportedConfigurationException(String.format("Project %s location matches workspace root %s", rootDir.getName(), workspaceRoot.getAbsolutePath()));
        }
    }

    private void saveProjectConfiguration(BuildConfiguration buildConfiguration, File rootDir, IProgressMonitor monitor) {
        ProjectConfiguration projectConfiguration = CorePlugin.configurationManager().createProjectConfiguration(buildConfiguration, rootDir);
        CorePlugin.configurationManager().saveProjectConfiguration(projectConfiguration);
    }

    private void importRootProject(File rootDir, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor);
        progress.setWorkRemaining(3);

        WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();
        Optional<IProject> projectOrNull = workspaceOperations.findProjectByLocation(rootDir);

        if (!projectOrNull.isPresent() && this.newProjectHandler.shouldImportNewProjects()) {
            String projectName = findFreeProjectName(rootDir.getName());
            projectOrNull = Optional.of(workspaceOperations.createProject(projectName, rootDir, ImmutableList.<String>of(), progress.newChild(1)));
        }

        if (projectOrNull.isPresent()) {
            IProject project = projectOrNull.get();
            project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(1));
            CorePlugin.workspaceOperations().addNature(project, GradleProjectNature.ID, progress.newChild(1));
            this.newProjectHandler.afterProjectImported(project);
            progress.worked(1);
        }
    }

    private String findFreeProjectName(String baseName) {
        Optional<IProject> workspaceProject = CorePlugin.workspaceOperations().findProjectByName(baseName);
        return workspaceProject.isPresent() ? findFreeProjectName(baseName + "_") : baseName;
    }

    /**
     * Exception type marking root import failure.
     */
    public static class ImportRootProjectException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ImportRootProjectException(Exception cause) {
            super(cause);
        }
    }
}
