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

package com.gradleware.tooling.eclipse.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.gradleware.tooling.eclipse.core.CorePlugin;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;

/**
 * Nature definitions which can be associated with Gradle projects.
 * <p/>
 * All entries are defined as extensions of the <code>org.eclipse.core.resources.natures</code>
 * point in the plugin.xml.
 *
 */
public enum GradleProjectNatures {

    DEFAULT_NATURE("nature");

    private final String natureId;

    GradleProjectNatures(String natureId) {
        // the nature ID has to be in the following format: ${PLUGIN_ID}.${NATURE_ID}
        this.natureId = CorePlugin.PLUGIN_ID + "." + natureId;
    }

    public String getId() {
        return this.natureId;
    }

    /**
     * Determines if the target project has the nature applied.
     *
     * @param project the project to verify
     * @return {@code true} if the specified project has the nature applied
     */
    public boolean isPresentOn(IProject project) {
        // abort if the project is closed since we cannot investigate closed projects
        if (!project.isOpen()) {
            String message = String.format("Cannot investigate Gradle nature on closed project %s.", project);
            CorePlugin.logger().error(message);
            throw new GradlePluginsRuntimeException(message);
        }

        // check if the Gradle nature is applied
        try {
            return project.hasNature(this.natureId);
        } catch (CoreException e) {
            String message = String.format("Cannot check for Gradle nature on project %s.", project);
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

}
