/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.io.File;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.util.binding.Property;

/**
 * Serves as the extra data model of the project creation wizard.
 */
public final class ProjectCreationConfiguration {

    private final Property<String> projectName;
    private final Property<Boolean> useDefaultLocation;
    private final Property<File> customLocation;
    private final Property<File> targetProjectDir;

    public ProjectCreationConfiguration(Property<String> projectName, Property<Boolean> useDefaultLocation, Property<File> customLocation, Property<File> targetProjectDir) {
        this.projectName = Preconditions.checkNotNull(projectName);
        this.useDefaultLocation = Preconditions.checkNotNull(useDefaultLocation);
        this.customLocation = Preconditions.checkNotNull(customLocation);
        this.targetProjectDir = Preconditions.checkNotNull(targetProjectDir);
    }

    public Property<String> getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName.setValue(projectName);
    }

    public Property<Boolean> getUseDefaultLocation() {
        return this.useDefaultLocation;
    }

    public void setUseDefaultLocation(boolean useDefaultLocation) {
        this.useDefaultLocation.setValue(useDefaultLocation);
    }

    public Property<File> getCustomLocation() {
        return this.customLocation;
    }

    public void setCustomLocation(File customLocation) {
        this.customLocation.setValue(customLocation);
    }

    public Property<File> getTargetProjectDir() {
        return this.targetProjectDir;
    }

    public void setTargetProjectDir(File targetProjectDir) {
        this.targetProjectDir.setValue(targetProjectDir);
    }

}
