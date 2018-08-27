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

package org.eclipse.buildship.core.internal.workspace;

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
        public boolean shouldImportNewProjects() {
            return false;
        };

        @Override
        public void afterProjectImported(IProject project) {
        }
    };

    /**
     * Will import new projects and keep their existing descriptors.
     */
    NewProjectHandler IMPORT_AND_MERGE = new NewProjectHandler() {

        @Override
        public boolean shouldImportNewProjects() {
            return true;
        };

        @Override
        public void afterProjectImported(IProject project) {
        }
    };

    /**
     * Determines whether the given project that was found in the Gradle model should be imported
     * into the workspace.
     *
     * @return true if the project should be imported, false otherwise
     */
    boolean shouldImportNewProjects();

    /**
     * Called after a project is newly imported into the workspace and all Gradle configuration has
     * been applied.
     *
     * @param project the newly imported project
     */
    void afterProjectImported(IProject project);

}
