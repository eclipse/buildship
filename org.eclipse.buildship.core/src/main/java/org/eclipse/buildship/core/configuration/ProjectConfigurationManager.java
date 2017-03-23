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

package org.eclipse.buildship.core.configuration;

import java.io.File;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.IProject;

/**
 * Manages the persisted configuration of Gradle projects in the Eclipse workspace.
 */
public interface ProjectConfigurationManager {

    /**
     * Returns the complete set of {@link ProjectConfiguration} instances found in the workspace.
     * <p/>
     * If a project configuration cannot be read, then the project is omitted from the result.
     *
     * @return the complete set of {@code ProjectConfiguration} instances
     */
    ImmutableSet<ProjectConfiguration> getAllProjectConfigurations();

    /**
     * Saves the target project configuration.
     *
     * @param project the target project
     * @param config the Gradle configuration to persist
     */
    void saveProjectConfiguration(ProjectConfiguration config);

    /**
     * Associates the target project configuration to a set of projects
     *
     * @param projectDirs the project directories to which the configuration should be associated
     * @param config the configuration to save
     */
    void attachProjectsToConfiguration(Set<File> projectDirs, ProjectConfiguration config);

    /**
     * Removes the association from a target project to the referenced project configuration
     *
     * @param project the target project
     */
    void detachProjectConfiguration(IProject project);

    /**
     * Reads the Gradle project configuration.
     *
     * @param project the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration
     */
    ProjectConfiguration readProjectConfiguration(IProject project);

    /**
     * Tries to read the Gradle project configuration. If the configuration is not present or cannot
     * be read, then the method returns {@code Optional#absent()}.
     *
     * @param project the project directory from which to read the Gradle configuration
     * @return the persisted Gradle configuration or {@code Optional#absent()} if the configuration
     *         cannot be read.
     */
    Optional<ProjectConfiguration> tryReadProjectConfiguration(IProject project);

    /**
     * Tries to read the Gradle project configuration. If the configuration is not present or cannot
     * be read, then the method returns {@code Optional#absent()}.
     *
     * @param project the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration or {@code Optional#absent()} if the configuration
     *         cannot be read.
     */
    Optional<ProjectConfiguration> tryReadProjectConfiguration(File projectDir);

    /**
     * Deletes the Gradle project configuration.
     *
     * @param project the Eclipse project from which to delete the Gradle configuration
     */
    void deleteProjectConfiguration(IProject project);
}
