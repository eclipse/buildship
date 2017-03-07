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
    public ImmutableSet<ProjectConfiguration> getRootProjectConfigurations() {
        // collect all Gradle root project configurations in the workspace by asking each Eclipse
        // project with a Gradle nature for the Gradle root project it belongs to
        ImmutableSet.Builder<ProjectConfiguration> rootConfigurations = ImmutableSet.builder();
        for (IProject workspaceProject : this.workspaceOperations.getAllProjects()) {
            if (GradleProjectNature.isPresentOn(workspaceProject)) {
                // calculate the root configuration to which the current configuration belongs
                Optional<ProjectConfiguration> projectConfiguration = tryReadProjectConfiguration(workspaceProject);
                if (projectConfiguration.isPresent()) {
                    ProjectConfiguration config = projectConfiguration.get();
                    if (config.getRootProjectDirectory().equals(workspaceProject.getLocation().toFile())) {
                        rootConfigurations.add(config);
                    }
                }
            }
        }

        // return the validated, unique set of root project configurations
        return rootConfigurations.build();
    }

    @Override
    public ImmutableSet<ProjectConfiguration> getAllProjectConfigurations() {
        // collect all the Gradle project configurations in the workspace
        ImmutableSet.Builder<ProjectConfiguration> allConfigurations = ImmutableSet.builder();
        for (IProject workspaceProject : this.workspaceOperations.getAllProjects()) {
            if (GradleProjectNature.isPresentOn(workspaceProject)) {
                Optional<ProjectConfiguration> projectConfiguration = tryReadProjectConfiguration(workspaceProject);
                if (projectConfiguration.isPresent()) {
                    allConfigurations.add(projectConfiguration.get());
                }
            }
        }

        // return the complete set of project configurations
        return allConfigurations.build();
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration) {
        this.projectConfigurationPersistence.saveProjectConfiguration(projectConfiguration);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject workspaceProject) {
        return tryReadProjectConfiguration(workspaceProject, false);
    }

    @Override
    public Optional<ProjectConfiguration> tryReadProjectConfiguration(IProject project) {
        ProjectConfiguration configuration = tryReadProjectConfiguration(project, true);
        return Optional.fromNullable(configuration);
    }

    private ProjectConfiguration tryReadProjectConfiguration(IProject workspaceProject, boolean suppressErrors) {
        try {
            return this.projectConfigurationPersistence.readProjectConfiguration(workspaceProject);
        } catch (RuntimeException e) {
            if (suppressErrors) {
                CorePlugin.logger().debug(String.format("Cannot load project configuration for project %s.", workspaceProject.getName()), e);
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public void deleteProjectConfiguration(IProject workspaceProject) {
        this.projectConfigurationPersistence.deleteProjectConfiguration(workspaceProject);
    }

}
