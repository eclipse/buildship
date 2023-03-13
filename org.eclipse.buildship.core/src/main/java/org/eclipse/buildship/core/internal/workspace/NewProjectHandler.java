/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
