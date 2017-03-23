/*
 * Copyright (c) 2016 the original author or authors.
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

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Internal contract how to read and write project configurations.
 */
interface ProjectConfigurationPersistence {

    void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject project);

    void saveProjectConfiguration(ProjectConfiguration projectConfiguration, File projectDir);

    void saveRootProjectLocation(IProject project, File rootProjectDir);

    void saveRootProjectLocation(File projectDir, File rootProjectDir);

    void deleteRootProjectLocation(IProject project);

    ProjectConfiguration readProjectConfiguration(IProject project);

    ProjectConfiguration readProjectConfiguration(File projectDir);

    File readRootProjectLocation(IProject project);

    File readRootProjectLocation(File projectDir);

    void deleteProjectConfiguration(IProject project);

    void deleteProjectConfiguration(File projectDir);
}
