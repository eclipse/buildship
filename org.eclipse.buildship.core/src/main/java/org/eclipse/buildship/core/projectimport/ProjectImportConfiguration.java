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

import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.gradle.GradleDistributionWrapper;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.Validator;
import com.gradleware.tooling.toolingutils.binding.Validators;

/**
 * Serves as the data model of the project import wizard.
 */
public final class ProjectImportConfiguration {

    private final Property<File> projectDir;
    private final Property<GradleDistributionWrapper> gradleDistribution;
    private final Property<File> gradleUserHome;
    private final Property<File> javaHome;
    private final Property<String> jvmArguments;
    private final Property<String> arguments;

    public ProjectImportConfiguration() {
        this(Validators.<File> noOp(), Validators.<GradleDistributionWrapper> noOp(), Validators.<File> noOp(), Validators.<File> noOp(), Validators.<String> noOp(), Validators
                .<String> noOp());
    }

    public ProjectImportConfiguration(Validator<File> projectDirValidator, Validator<GradleDistributionWrapper> gradleDistributionValidator,
            Validator<File> gradleUserHomeValidator, Validator<File> javaHomeValidator, Validator<String> jvmArgumentsValidator, Validator<String> argumentsValidator) {
        this.projectDir = Property.create(projectDirValidator);
        this.gradleDistribution = Property.create(gradleDistributionValidator);
        this.gradleUserHome = Property.create(gradleUserHomeValidator);
        this.javaHome = Property.create(javaHomeValidator);
        this.jvmArguments = Property.create(jvmArgumentsValidator);
        this.arguments = Property.create(argumentsValidator);
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

    public Property<File> getJavaHome() {
        return this.javaHome;
    }

    public void setJavaHome(File javaHome) {
        this.javaHome.setValue(javaHome);
    }

    public Property<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments.setValue(jvmArguments);
    }

    public Property<String> getArguments() {
        return this.arguments;
    }

    public void setArguments(String arguments) {
        this.arguments.setValue(arguments);
    }

    public FixedRequestAttributes toFixedAttributes() {
        File projectDir = getProjectDir().getValue();
        GradleDistribution gradleDistribution = getGradleDistribution().getValue().toGradleDistribution();
        File gradleUserHome = getGradleUserHome().getValue();
        File javaHome = getJavaHome().getValue();
        List<String> jvmArguments = CollectionsUtils.splitBySpace(getJvmArguments().getValue());
        List<String> arguments = CollectionsUtils.splitBySpace(getArguments().getValue());

        return new FixedRequestAttributes(projectDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
    }

}
