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

package org.eclipse.buildship.ui.wizard.project;

import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.Validator;

import java.io.File;

/**
 * Serves as the extra data model of the project creation wizard.
 */
public final class ProjectCreationConfiguration {

    private final Property<String> projectName;
    private final Property<Boolean> useDefaultLocation;
    private final Property<File> customLocation;
    private final Property<File> targetProjectDir;

    public ProjectCreationConfiguration(Validator<String> projectNameValidator, Validator<Boolean> useDefaultLocationValidator, Validator<File> customLocationValidator, Validator<File> targetProjectDirValidator) {
        this.projectName = Property.create(projectNameValidator);
        this.useDefaultLocation = Property.create(useDefaultLocationValidator);
        this.customLocation = Property.create(customLocationValidator);
        this.targetProjectDir = Property.create(targetProjectDirValidator);
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
