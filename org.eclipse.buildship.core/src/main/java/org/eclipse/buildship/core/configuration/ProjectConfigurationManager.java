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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.IProject;

/**
 * Manages the persisted configuration of Gradle projects in the Eclipse workspace.
 */
public interface ProjectConfigurationManager {

    /**
     * Returns the unique set of {@link ProjectConfiguration} roots found in the workspace.
     * <p/>
     * If a project configuration cannot be read, then the project is omitted from the result.
     *
     * @return the unique set of {@code ProjectConfiguration} roots
     */
    ImmutableSet<ProjectConfiguration> getRootProjectConfigurations();

    /**
     * Returns the complete set of {@link ProjectConfiguration} instances found in the workspace.
     * <p/>
     * If a project configuration cannot be read, then the project is omitted from the result.
     *
     * @return the complete set of {@code ProjectConfiguration} instances
     */
    ImmutableSet<ProjectConfiguration> getAllProjectConfigurations();

    /**
     * Saves the given Gradle project configuration in the Eclipse project's <i>.settings</i>
     * folder.
     *
     * @param projectConfiguration the Gradle configuration to persist
     */
    void saveProjectConfiguration(ProjectConfiguration projectConfiguration);

    /**
     * Reads the Gradle project configuration from the Eclipse project's <i>.settings</i> folder.
     *
     * @param workspaceProject the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration
     */
    ProjectConfiguration readProjectConfiguration(IProject workspaceProject);

    /**
     * Tries to read the Gradle project configuration from the Eclipse project's <i>.settings</i>
     * folder. If the configuration is not present or cannot be read, then the method returns
     * {@code Optional#absent()}.
     *
     * @param workspaceProject the Eclipse project from which to read the Gradle configuration
     * @return the persisted Gradle configuration or {@code Optional#absent()} if the configuration
     *         cannot be read.
     */
    Optional<ProjectConfiguration> tryReadProjectConfiguration(IProject workspaceProject);

    /**
     * Deletes the Gradle project configuration from the Eclipse project's <i>.settings</i> folder.
     *
     * @param workspaceProject the Eclipse project from which to delete the Gradle configuration
     */
    void deleteProjectConfiguration(IProject workspaceProject);

}
