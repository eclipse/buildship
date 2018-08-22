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

package org.eclipse.buildship.core.internal.configuration.impl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectMarker;

/**
 * Validates the project configuration when a relevant resources changes.
 */
final class GradleProjectValidationResourceDeltaVisitor implements IResourceDeltaVisitor {

    private final GradleProjectValidator gradleProjectValidator;

    GradleProjectValidationResourceDeltaVisitor(IProject project) {
        this.gradleProjectValidator = new GradleProjectValidator(project);
    }

    /**
     * Forces validation of the project.
     *
     * @return {@code true} if the validation succeeds, {@code false} otherwise
     * @throws CoreException thrown if there is a problem with the marker operations
     */
    public boolean validate() throws CoreException {
        return this.gradleProjectValidator.validate();
    }

    /**
     * Triggers validation of the project if the resource delta affects the project configuration.
     *
     * @param delta the changed resource
     * @return {@code true} if the resource delta's children should be visited
     */
    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        IPath settingsFolderProjectRelativePath = this.gradleProjectValidator.getSettingsFolder().getProjectRelativePath();
        IPath preferencesFileProjectRelativePath = this.gradleProjectValidator.getPreferencesFile().getProjectRelativePath();

        if (delta.getProjectRelativePath().equals(settingsFolderProjectRelativePath)) {
            if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED) {
                // handle the use case when the .settings folder is added or removed
                // (this also covers the use case of renaming the .settings folder to something else)
                validate();
                return false;
            }
        } else if (delta.getProjectRelativePath().equals(preferencesFileProjectRelativePath)) {
            if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.CHANGED) {
                // handle the use case when the gradle.prefs file is added or removed or changed
                // (this also covers the use case of renaming the gradle.prefs file to something else)
                validate();
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the Gradle project configuration and adds error markers to the given {@link org.eclipse.core.resources.IProject} instance if the configuration is missing or
     * invalid.
     */
    private static final class GradleProjectValidator {

        private final IProject project;
        private final IFolder settingsFolder;
        private final IFile preferencesFile;

        private GradleProjectValidator(IProject project) {
            this.project = project;
            this.settingsFolder = project.getFolder(".settings");
            this.preferencesFile = project.getFile(".settings/" + CorePlugin.PLUGIN_ID + ".prefs");
        }

        public IFolder getSettingsFolder() {
            return this.settingsFolder;
        }

        public IFile getPreferencesFile() {
            return this.preferencesFile;
        }

        /**
         * Validates the project configuration and sets error markers if needed.
         *
         * @return {@code true} if the validation succeeds, {@code false} otherwise
         * @throws CoreException thrown if there is a problem with the marker operations
         */
        public boolean validate() throws CoreException {
            // initially, remove all markers from the project and the preference file
            GradleProjectMarker.INSTANCE.removeMarkerFromResourceRecursively(this.project);

            // todo (etst) extract i18n keys

            //CHECKSTYLE:OFF, required due to false negative in Checkstyle
            // validate the existence of the .settings folder
            if (!this.settingsFolder.exists()) {
                String message = String.format("Missing Gradle project configuration folder: %s", this.settingsFolder.getProjectRelativePath());
                GradleProjectMarker.INSTANCE.addMarkerToResource(this.project, message);
                return false;
            }
            // validate the existence of the .settings/gradle.prefs file
            else if (!this.preferencesFile.exists()) {
                String message = String.format("Missing Gradle project configuration file: %s", this.preferencesFile.getProjectRelativePath());
                GradleProjectMarker.INSTANCE.addMarkerToResource(this.project, message);
                return false;
            }
            // validate the state of the .settings/gradle.prefs file
            else {
                try {
                    CorePlugin.configurationManager().loadProjectConfiguration(this.project);
                } catch (Exception e) {
                    String message = String.format("Invalid Gradle project configuration file: %s", this.preferencesFile.getProjectRelativePath());
                    GradleProjectMarker.INSTANCE.addMarkerToResource(this.preferencesFile, message);
                    return false;
                }
            }
            //CHECKSTYLE:ON

            return true;
        }

    }

}
