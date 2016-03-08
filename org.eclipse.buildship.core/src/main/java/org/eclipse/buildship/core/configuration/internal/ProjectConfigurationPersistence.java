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

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Internal contract how to read and write project configurations.
 */
interface ProjectConfigurationPersistence {
    void saveProjectConfiguration(ProjectConfiguration projectConfiguration, IProject workspaceProject);
    void deleteProjectConfiguration(IProject workspaceProject);
    ProjectConfiguration readProjectConfiguration(IProject workspaceProject);

}
