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

package com.gradleware.tooling.eclipse.core.configuration.internal;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.gradleware.tooling.eclipse.core.CorePlugin;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.configuration.ProjectConfiguration;
import com.gradleware.tooling.eclipse.core.configuration.ProjectConfigurationManager;
import com.gradleware.tooling.eclipse.core.project.GradleProjectNatures;
import com.gradleware.tooling.eclipse.core.workspace.WorkspaceOperations;
import com.gradleware.tooling.toolingmodel.Path;

/**
 * Manages the persistence and querying of information related to {@code ProjectConfiguration}s.
 */
public final class DefaultProjectConfigurationManager implements ProjectConfigurationManager {

    private final WorkspaceOperations workspaceOperations;
    private final ProjectConfigurationPersistence projectConfigurationPersistence;

    public DefaultProjectConfigurationManager(WorkspaceOperations workspaceOperations) {
        this.workspaceOperations = workspaceOperations;
        this.projectConfigurationPersistence = new ProjectConfigurationPersistence();
    }

    @Override
    public ImmutableSet<ProjectConfiguration> getRootProjectConfigurations() {
        // collect all Gradle root project configurations in the workspace by asking each Eclipse
        // project with a Gradle nature for the Gradle root project it belongs to
        ImmutableSet.Builder<ProjectConfiguration> rootConfigurations = ImmutableSet.builder();
        for (IProject workspaceProject : this.workspaceOperations.getAllProjects()) {
            if (workspaceProject.isOpen() && GradleProjectNatures.DEFAULT_NATURE.isPresentOn(workspaceProject)) {
                // calculate the root configuration to which the current configuration belongs
                ProjectConfiguration projectConfiguration = this.projectConfigurationPersistence.readProjectConfiguration(workspaceProject);
                File rootProjectDir = projectConfiguration.getRequestAttributes().getProjectDir();
                ProjectConfiguration rootProjectConfiguration = ProjectConfiguration.from(projectConfiguration.getRequestAttributes(), Path.from(":"), rootProjectDir);
                rootConfigurations.add(rootProjectConfiguration);
            }
        }

        // make sure there are no projects that point to the same root project but with a different
        // configuration (different java home, etc.)
        // if such an inconsistent state is detected, it means that the Gradle configurations were
        // changed/corrupted manually
        Map<String, ProjectConfiguration> rootProjectDirs = Maps.newHashMap();
        for (ProjectConfiguration rootProjectConfiguration : rootConfigurations.build()) {
            String rootProjectDirPath = rootProjectConfiguration.getProjectDir().getPath();
            if (!rootProjectDirs.containsKey(rootProjectDirPath)) {
                rootProjectDirs.put(rootProjectDirPath, rootProjectConfiguration);
            } else {
                String message = String.format("Inconsistent Gradle project configuration for project at %s.", rootProjectDirPath);
                CorePlugin.logger().error(message);
                throw new GradlePluginsRuntimeException(message);
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
            if (workspaceProject.isOpen() && GradleProjectNatures.DEFAULT_NATURE.isPresentOn(workspaceProject)) {
                ProjectConfiguration projectConfiguration = this.projectConfigurationPersistence.readProjectConfiguration(workspaceProject);
                allConfigurations.add(projectConfiguration);
            }
        }

        // return the complete set of project configurations
        return allConfigurations.build();
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject workspaceProject) {
        this.projectConfigurationPersistence.saveProjectConfiguration(projectConfiguration, workspaceProject);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject workspaceProject) {
        return this.projectConfigurationPersistence.readProjectConfiguration(workspaceProject);
    }

}
