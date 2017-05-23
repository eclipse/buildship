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

package org.eclipse.buildship.core.projectimport;

import java.io.File;
import java.util.List;

import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.Validator;
import com.gradleware.tooling.toolingutils.binding.Validators;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;

/**
 * Serves as the data model of the project import wizard.
 */
public final class ProjectImportConfiguration {

    private final Property<File> projectDir;
    private final Property<GradleDistributionWrapper> gradleDistribution;
    private final Property<File> gradleUserHome;
    private final Property<Boolean> applyWorkingSets;
    private final Property<List<String>> workingSets;

    public ProjectImportConfiguration() {
        this(Validators.<File>noOp(), Validators.<GradleDistributionWrapper>noOp(), Validators.<File>noOp(), Validators.<Boolean>noOp(), Validators.<List<String>>noOp());
    }

    public ProjectImportConfiguration(Validator<File> projectDirValidator, Validator<GradleDistributionWrapper> gradleDistributionValidator,
            Validator<File> gradleUserHomeValidator,
            Validator<Boolean> applyWorkingSetsValidator, Validator<List<String>> workingSetsValidators) {
        this.projectDir = Property.create(projectDirValidator);
        this.gradleDistribution = Property.create(gradleDistributionValidator);
        this.gradleUserHome = Property.create(gradleUserHomeValidator);
        this.applyWorkingSets = Property.create(applyWorkingSetsValidator);
        this.workingSets = Property.create(workingSetsValidators);
    }

    public Property<File> getProjectDir() {
        return this.projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir.setValue(projectDir);
    }

    public Property<GradleDistributionWrapper> getGradleDistribution() {
        return this.gradleDistribution;
    }

    public void setGradleDistribution(GradleDistributionWrapper gradleDistribution) {
        this.gradleDistribution.setValue(gradleDistribution);
    }

    public Property<File> getGradleUserHome() {
        return this.gradleUserHome;
    }

    public void setGradleUserHome(File gradleUserHome) {
        this.gradleUserHome.setValue(gradleUserHome);
    }

    public Property<Boolean> getApplyWorkingSets() {
        return this.applyWorkingSets;
    }

    public void setApplyWorkingSets(Boolean applyWorkingSets) {
        this.applyWorkingSets.setValue(applyWorkingSets);
    }

    public Property<List<String>> getWorkingSets() {
        return this.workingSets;
    }

    public void setWorkingSets(List<String> workingSets) {
        this.workingSets.setValue(workingSets);
    }

    public BuildConfiguration toBuildConfig() {
        return CorePlugin.configurationManager().createBuildConfiguration(getProjectDir().getValue(), getGradleDistribution().getValue().toGradleDistribution(), null, false, false, false);
    }
}
