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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Project nature for Gradle projects.
 */
public enum GradleProjectNature {

    INSTANCE;

    // the nature ID has to be in the following format: ${PLUGIN_ID}.${NATURE_ID}
    public static final String ID = CorePlugin.PLUGIN_ID + ".gradleprojectnature";

    /**
     * Determines if the target project has the Gradle nature applied.
     *
     * @param project the project to verify
     * @return {@code true} if the specified project has the nature applied
     */
    public boolean isPresentOn(IProject project) {
        // abort if the project is not open since we can only investigate open projects
        if (!project.isOpen()) {
            throw new GradlePluginsRuntimeException(String.format("Cannot investigate Gradle nature on closed project %s.", project));
        }

        // check if the Gradle nature is applied
        try {
            return project.hasNature(ID);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(String.format("Cannot check for Gradle nature on project %s.", project), e);
        }
    }

}
