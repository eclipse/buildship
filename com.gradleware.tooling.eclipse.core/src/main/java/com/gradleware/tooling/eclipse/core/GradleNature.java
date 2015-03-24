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

package com.gradleware.tooling.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Gradle nature definition registered via the <code>org.eclipse.core.resources.natures</code>
 * extension point in the plugin.xml.
 */
public final class GradleNature implements IProjectNature {

    // the nature ID has to be in the following format: ${PLUGIN_ID}.${NATURE_ID}
    // the nature id is defined in the external (xml) file specified in the plugin.xml
    public static final String ID = CorePlugin.PLUGIN_ID + ".nature";

    private IProject project;

    @Override
    public void configure() {
    }

    @Override
    public void deconfigure() {
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    /**
     * Determines if the specified project has the Gradle nature applied.
     *
     * @param project the project to verify
     * @return {@code true} iff the specified project has the Gradle nature applied
     */
    public static boolean isPresentOn(IProject project) {
        // abort if the project is closed since we cannot investigate closed projects
        if (!project.isOpen()) {
            String message = String.format("Cannot investigate Gradle nature on closed project %s.", project);
            CorePlugin.logger().error(message);
            throw new GradlePluginsRuntimeException(message);
        }

        // check if the Gradle nature is applied
        try {
            return project.hasNature(ID);
        } catch (CoreException e) {
            String message = String.format("Cannot check for Gradle nature on project %s.", project);
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

}
