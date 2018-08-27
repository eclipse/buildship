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

package org.eclipse.buildship.core.internal.configuration;

import com.google.common.base.Predicate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Project nature for Gradle projects.
 */
public final class GradleProjectNature {

    // the nature ID has to be in the following format: ${PLUGIN_ID}.${NATURE_ID}
    public static final String ID = CorePlugin.PLUGIN_ID + ".gradleprojectnature";

    /**
     * A predicate that can be used to filter projects based on whether they have the Gradle nature.
     * @return the predicate
     */
    public static Predicate<IProject> isPresentOn() {
        return new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return GradleProjectNature.isPresentOn(project);
            }
        };
    }

    /**
     * Determines if the target project has the Gradle nature applied.
     *
     * @param project the project to verify, can be null
     * @return {@code true} if the specified project has the nature applied
     */
    public static boolean isPresentOn(IProject project) {
        if (project == null) {
            return false;
        }
        if (!project.isOpen()) {
            return false;
        }

        try {
            return project.hasNature(ID);
        } catch (CoreException e) {
            return false;
        }
    }

    private GradleProjectNature() {
    }

}
