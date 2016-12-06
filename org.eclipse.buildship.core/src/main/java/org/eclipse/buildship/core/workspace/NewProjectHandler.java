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

package org.eclipse.buildship.core.workspace;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;

import org.eclipse.core.resources.IProject;

/**
 * Decides what to do when new projects are discovered during a Gradle synchronize operation.
 */
public interface NewProjectHandler {

    /**
     * Will not import any new projects.
     */
    NewProjectHandler NO_OP = new NewProjectHandler() {

        @Override
        public boolean shouldImport(OmniEclipseProject projectModel) {
            return false;
        };

        @Override
        public void afterImport(IProject project, OmniEclipseProject projectModel) {
        }
    };

    /**
     * Will import new projects and keep their existing descriptors.
     */
    NewProjectHandler IMPORT_AND_MERGE = new NewProjectHandler() {

        @Override
        public boolean shouldImport(OmniEclipseProject projectModel) {
            return true;
        };

        @Override
        public void afterImport(IProject project, OmniEclipseProject projectModel) {
        }
    };

    /**
     * Determines whether the given project that was found in the Gradle model should be imported
     * into the workspace.
     *
     * @param projectModel the Gradle model of the project
     * @return true if the project should be imported, false otherwise
     */
    boolean shouldImport(OmniEclipseProject projectModel);

    /**
     * Called after a project is newly imported into the workspace and all Gradle configuration has
     * been applied.
     *
     * @param project the newly imported project
     * @param projectModel the Gradle model of the project
     */
    void afterImport(IProject project, OmniEclipseProject projectModel);

}
