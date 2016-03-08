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

import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingmodel.Path;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
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
        this.projectConfigurationPersistence = new LegacyCleaningProjectConfigurationPersistence(new DefaultProjectConfigurationPersistence());
    }

    @Override
    public ImmutableSet<ProjectConfiguration> getRootProjectConfigurations() {
        // collect all Gradle root project configurations in the workspace by asking each Eclipse
        // project with a Gradle nature for the Gradle root project it belongs to
        ImmutableSet.Builder<ProjectConfiguration> rootConfigurations = ImmutableSet.builder();
        for (IProject workspaceProject : this.workspaceOperations.getAllProjects()) {
            if (workspaceProject.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(workspaceProject)) {
                // calculate the root configuration to which the current configuration belongs
                ProjectConfiguration projectConfiguration = this.projectConfigurationPersistence.readProjectConfiguration(workspaceProject);
                ProjectConfiguration rootProjectConfiguration = ProjectConfiguration.from(projectConfiguration.getRequestAttributes(), Path.from(":"));
                rootConfigurations.add(rootProjectConfiguration);
            }
        }

        // make sure there are no projects that point to the same root project but with a different
        // configuration (different java home, etc.)
        // if such an inconsistent state is detected, it means that the Gradle configurations were
        // changed/corrupted manually
        Map<String, ProjectConfiguration> rootProjectDirs = Maps.newHashMap();
        for (ProjectConfiguration rootProjectConfiguration : rootConfigurations.build()) {
            String rootProjectDirPath = rootProjectConfiguration.getRequestAttributes().getProjectDir().getAbsolutePath();
            if (!rootProjectDirs.containsKey(rootProjectDirPath)) {
                rootProjectDirs.put(rootProjectDirPath, rootProjectConfiguration);
            } else {
                throw new GradlePluginsRuntimeException(String.format("Inconsistent Gradle project configuration for project at %s.", rootProjectDirPath));
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
            if (workspaceProject.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(workspaceProject)) {
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

    @Override
    public void deleteProjectConfiguration(IProject workspaceProject) {
        this.projectConfigurationPersistence.deleteProjectConfiguration(workspaceProject);
    }

}
