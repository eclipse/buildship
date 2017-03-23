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

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Manages the persistence and querying of information related to {@code ProjectConfiguration}s.
 */
public final class DefaultProjectConfigurationManager implements ProjectConfigurationManager {

    private final WorkspaceOperations workspaceOperations;
    private final ProjectConfigurationPersistence projectConfigurationPersistence;

    public DefaultProjectConfigurationManager(WorkspaceOperations workspaceOperations) {
        this.workspaceOperations = workspaceOperations;
        this.projectConfigurationPersistence = new DefaultProjectConfigurationPersistence();
    }

    @Override
    public ImmutableSet<ProjectConfiguration> getAllProjectConfigurations() {
        ImmutableSet.Builder<ProjectConfiguration> allConfigurations = ImmutableSet.builder();
        for (IProject workspaceProject : this.workspaceOperations.getAllProjects()) {
            if (GradleProjectNature.isPresentOn(workspaceProject)) {
                Optional<ProjectConfiguration> projectConfiguration = tryReadProjectConfiguration(workspaceProject);
                if (projectConfiguration.isPresent()) {
                    allConfigurations.add(projectConfiguration.get());
                }
            }
        }
        return allConfigurations.build();
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration) {
        File rootDir = projectConfiguration.getRootProjectDirectory();
        Optional<IProject> rootProject = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
        if (rootProject.isPresent()) {
            this.projectConfigurationPersistence.saveProjectConfiguration(projectConfiguration, rootProject.get());
        } else {
            this.projectConfigurationPersistence.saveProjectConfiguration(projectConfiguration, rootDir);
        }
    }

    @Override
    public void attachProjectsToConfiguration(Set<File> projectDirs, ProjectConfiguration projectConfiguration) {
        File rootDir = projectConfiguration.getRootProjectDirectory();
        for (File projectDir : projectDirs) {
            Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(projectDir);
            if (project.isPresent()) {
                this.projectConfigurationPersistence.saveRootProjectLocation(project.get(), rootDir);
            } else {
                this.projectConfigurationPersistence.saveRootProjectLocation(projectDir, rootDir);
            }
        }
    }

    @Override
    public void detachProjectConfiguration(IProject project) {
        this.projectConfigurationPersistence.deleteRootProjectLocation(project);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        return tryReadProjectConfiguration(project, false);
    }

    @Override
    public Optional<ProjectConfiguration> tryReadProjectConfiguration(IProject project) {
        ProjectConfiguration configuration = tryReadProjectConfiguration(project, true);
        return Optional.fromNullable(configuration);
    }

    @Override
    public Optional<ProjectConfiguration> tryReadProjectConfiguration(File projectDir) {
        ProjectConfiguration configuration = tryReadProjectConfiguration(projectDir, true);
        return Optional.fromNullable(configuration);
    }

    private ProjectConfiguration tryReadProjectConfiguration(IProject workspaceProject, boolean suppressErrors) {
        try {
            File rootDir = this.projectConfigurationPersistence.readRootProjectLocation(workspaceProject);
            Optional<IProject> rootProject = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
            if (rootProject.isPresent()) {
                return this.projectConfigurationPersistence.readProjectConfiguration(rootProject.get());
            } else {
                return this.projectConfigurationPersistence.readProjectConfiguration(rootDir);
            }
        } catch (RuntimeException e) {
            if (suppressErrors) {
                CorePlugin.logger().debug(String.format("Cannot load project configuration for project %s.", workspaceProject.getName()), e);
                return null;
            } else {
                throw e;
            }
        }
    }

    private ProjectConfiguration tryReadProjectConfiguration(File projectDir, boolean suppressErrors) {
        try {
            File rootDir = this.projectConfigurationPersistence.readRootProjectLocation(projectDir);
            Optional<IProject> rootProject = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
            if (rootProject.isPresent()) {
                return this.projectConfigurationPersistence.readProjectConfiguration(rootProject.get());
            } else {
                return this.projectConfigurationPersistence.readProjectConfiguration(rootDir);
            }
        } catch (RuntimeException e) {
            if (suppressErrors) {
                CorePlugin.logger().debug(String.format("Cannot load project configuration for project in %s.", projectDir.getAbsolutePath()), e);
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        File rootDir = this.projectConfigurationPersistence.readRootProjectLocation(project);
        Optional<IProject> rootProject = CorePlugin.workspaceOperations().findProjectByLocation(rootDir);
        if (rootProject.isPresent()) {
            this.projectConfigurationPersistence.deleteProjectConfiguration(rootProject.get());
        } else {
            this.projectConfigurationPersistence.deleteProjectConfiguration(rootDir);
        }
    }
}
