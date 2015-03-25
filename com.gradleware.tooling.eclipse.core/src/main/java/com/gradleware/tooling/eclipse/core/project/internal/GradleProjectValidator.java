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

package com.gradleware.tooling.eclipse.core.project.internal;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.project.GradleProjectMarkers;

public final class GradleProjectValidator {

    private final IProject project;
    private final IFolder dotSettingsFolder;
    private final IFile preferencesFile;

    public GradleProjectValidator(IProject project) {
        this.project = project;
        this.dotSettingsFolder = project.getFolder(".settings");
        this.preferencesFile = project.getFile(".settings/gradle.prefs");
    }

    public List<IResource> resourcesToValidate() {
        return ImmutableList.<IResource> of(this.dotSettingsFolder, this.preferencesFile);
    }

    public boolean validateProjectConfigurationExists() throws CoreException {
        // when validate, initially remove all markers from the project
        GradleProjectMarkers.MISSING_CONFIGURATION_MARKER.removeMarkerFromResource(this.project);

        // validate the existence of the project configuration files
        if (!this.dotSettingsFolder.exists()) {
            GradleProjectMarkers.MISSING_CONFIGURATION_MARKER.addMarkerToResource(this.project, "Missing project configuration folder: .settings");
        } else if (!this.preferencesFile.exists()) {
            GradleProjectMarkers.MISSING_CONFIGURATION_MARKER.addMarkerToResource(this.project, "Missing project configuration file: .settings/gradle.prefs");
        }
        return true;
    }
}
