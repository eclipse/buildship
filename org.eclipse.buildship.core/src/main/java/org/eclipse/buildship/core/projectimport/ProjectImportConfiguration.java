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

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.Validator;
import com.gradleware.tooling.toolingutils.binding.Validators;

import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;

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
    private final Property<List<String>> workingSets;
    private final Property<String> newProjectName;
    private final Property<File> newProjectLocation;
    private final Property<List<String>> possibleLocations;
    private final Property<Boolean> useWorkspaceLocation;

    public ProjectImportConfiguration() {
        this(Validators.<File>noOp(), Validators.<GradleDistributionWrapper>noOp(), Validators.<File>noOp(), Validators.<File>noOp(),
                Validators.<String>noOp(), Validators.<String>noOp(), Validators.<List<String>>noOp(), Validators.<String>noOp(), Validators.<File>noOp(), Validators.<List<String>>noOp());
    }

    public ProjectImportConfiguration(Validator<File> projectDirValidator, Validator<GradleDistributionWrapper> gradleDistributionValidator,
                                      Validator<File> gradleUserHomeValidator, Validator<File> javaHomeValidator, Validator<String> jvmArgumentsValidator,
                                      Validator<String> argumentsValidator, Validator<List<String>> workingSetsValidators,
                                      Validator<String> newProjectNameValidator, Validator<File> newProjectLocationValidator,
                                      Validator<List<String>> possibleLocationsValidator) {
        this.projectDir = Property.create(projectDirValidator);
        this.gradleDistribution = Property.create(gradleDistributionValidator);
        this.gradleUserHome = Property.create(gradleUserHomeValidator);
        this.javaHome = Property.create(javaHomeValidator);
        this.jvmArguments = Property.create(jvmArgumentsValidator);
        this.arguments = Property.create(argumentsValidator);
        this.workingSets = Property.create(workingSetsValidators);
        this.newProjectName= Property.create(newProjectNameValidator);
        this.newProjectLocation = Property.create(newProjectLocationValidator);
        this.possibleLocations = Property.create(possibleLocationsValidator);
        this.useWorkspaceLocation = Property.create(Validators.<Boolean>noOp());
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

    public Property<List<String>> getWorkingSets() {
        return this.workingSets;
    }

    public void setWorkingSets(List<String> workingSets) {
        this.workingSets.setValue(workingSets);
    }

    public Property<String> getNewProjectName() {
        return this.newProjectName;
    }

    public void setNewProjectName(String newProjectName) {
        this.newProjectName.setValue(newProjectName);
    }

    public Property<File> getNewProjectLocation() {
        return this.newProjectLocation;
    }

    public void setNewProjectLocation(File newProjectLocation) {
        this.newProjectLocation.setValue(newProjectLocation);
    }

    public Property<List<String>> getPossibleLocations() {
        return this.possibleLocations;
    }

    public void setPossibleLocations(List<String> possibleLocations) {
        this.possibleLocations.setValue(possibleLocations);
    }

    public Property<Boolean> getUseWorkspaceLocation() {
        return this.useWorkspaceLocation;
    }

    public void setUseWorkspaceLocation(Boolean useWorkspaceLocation) {
        this.useWorkspaceLocation.setValue(useWorkspaceLocation);
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
