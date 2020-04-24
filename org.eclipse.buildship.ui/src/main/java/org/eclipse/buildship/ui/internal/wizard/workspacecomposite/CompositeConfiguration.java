/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.CompositeProperties;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Serves as the data model of the composite import wizard.
 */
public final class CompositeConfiguration {

    private final Property<File> compositePreferencesDir;
    private final Property<IAdaptable[]> projectList;
    private final Property<Boolean> overwriteWorkspaceSettings;
    private final Property<GradleDistributionViewModel> distribution;
    private final Property<File> gradleUserHome;
    private final Property<File> javaHome;
    private final Property<Boolean> applyWorkingSets;
    private final Property<List<String>> workingSets;
    private final Property<Boolean> buildScansEnabled;
    private final Property<Boolean> offlineMode;
    private final Property<Boolean> autoSync;
    private final Property<List<String>> arguments;
    private final Property<List<String>> jvmArguments;
    private final Property<Boolean> showConsoleView;
    private final Property<Boolean> showExecutionsView;
    private final Property<Boolean> projectAsCompositeRoot;
    private final Property<File> rootProject;

    public CompositeConfiguration() {
        this(Validators.<File>noOp(), Validators.<GradleDistributionViewModel>noOp(), Validators.<File>noOp(), Validators.<File>noOp(), Validators.<Boolean>noOp(), Validators.<List<String>>noOp(), Validators.<File>noOp());
    }

    public CompositeConfiguration(Validator<File> compositePreferencesDirValidator, Validator<GradleDistributionViewModel> distributionValidator,
            Validator<File> gradleUserHomeValidator, Validator<File> javaHomeValidator, Validator<Boolean> applyWorkingSetsValidator, Validator<List<String>> workingSetsValidators, Validator<File> rootProjectValidator) {
        this.compositePreferencesDir = Property.create(compositePreferencesDirValidator);
        this.projectList = Property.<IAdaptable[]>create(Validators.<IAdaptable[]>noOp());
        this.overwriteWorkspaceSettings = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.distribution = Property.create(distributionValidator);
        this.gradleUserHome = Property.create(gradleUserHomeValidator);
        this.javaHome = Property.create(javaHomeValidator);
        this.applyWorkingSets = Property.create(applyWorkingSetsValidator);
        this.workingSets = Property.create(workingSetsValidators);
        this.buildScansEnabled = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.offlineMode = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.autoSync = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.arguments = Property.<List<String>>create(Validators.<List<String>>noOp());
        this.jvmArguments = Property.<List<String>>create(Validators.<List<String>>noOp());
        this.showConsoleView = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.showExecutionsView = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.projectAsCompositeRoot = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.rootProject = Property.create(rootProjectValidator);

    }

    public Property<File> getCompositePreferencesDir() {
        return this.compositePreferencesDir;
    }

    public void setCompositePreferencesDir(File compositePreferencesDir) {
        this.compositePreferencesDir.setValue(compositePreferencesDir);
    }

    public Property<IAdaptable[]> getProjectList() {
    	return this.projectList;
    }

    public void setProjectList(IAdaptable[] projectList) {
    	this.projectList.setValue(projectList);
    }

    public Property<Boolean> getOverrideWorkspaceConfiguration() {
        return this.overwriteWorkspaceSettings;
    }

    public void setOverwriteWorkspaceSettings(boolean overwriteWorkspaceSettings) {
        this.overwriteWorkspaceSettings.setValue(Boolean.valueOf(overwriteWorkspaceSettings));
    }

    public Property<GradleDistributionViewModel> getDistribution() {
        return this.distribution;
    }

    public void setDistribution(GradleDistributionViewModel distribution) {
        this.distribution.setValue(distribution);
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

    public void setJavaHomeHome(File javaHome) {
        this.javaHome.setValue(javaHome);
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

    public Property<Boolean> getBuildScansEnabled() {
        return this.buildScansEnabled;
    }

    public void setBuildScansEnabled(boolean buildScansEnabled) {
        this.buildScansEnabled.setValue(Boolean.valueOf(buildScansEnabled));
    }

    public Property<Boolean> getOfflineMode() {
        return this.offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode.setValue(Boolean.valueOf(offlineMode));
    }

    public Property<Boolean> getAutoSync() {
        return this.autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync.setValue(Boolean.valueOf(autoSync));
    }

    public Property<List<String>> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments.setValue(arguments);
    }

    public Property<List<String>> getJvmArguments() {
        return this.jvmArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments.setValue(jvmArguments);
    }

    public Property<Boolean> getShowConsoleView() {
        return this.showConsoleView;
    }

    public void setShowConsoleView(boolean showConsoleView) {
        this.showConsoleView.setValue(Boolean.valueOf(showConsoleView));
    }

    public Property<Boolean> getShowExecutionsView() {
        return this.showExecutionsView;
    }

    public void setShowExecutionsView(boolean showExecutionsView) {
        this.showExecutionsView.setValue(Boolean.valueOf(showExecutionsView));
    }

    public Property<Boolean> getProjectAsCompositeRoot() {
        return this.projectAsCompositeRoot;
    }

    public void setProjectAsCompositeRoot(boolean overwriteRootProject) {
        this.projectAsCompositeRoot.setValue(Boolean.valueOf(overwriteRootProject));
    }

    public Property<File> getRootProject() {
        return this.rootProject;
    }

    public void setRootProject(File rootProject) {
        this.rootProject.setValue(rootProject);
    }

    public CompositeProperties toCompositeProperties() {
    	return CompositeProperties.forRootProjectDirectory(getCompositePreferencesDir().getValue())
    			.projectList(getProjectList().getValue())
    			.overrideWorkspaceConfiguration(getOverrideWorkspaceConfiguration().getValue())
    			.gradleDistribution(getDistribution().getValue().toGradleDistribution())
    			.gradleUserHome(getGradleUserHome().getValue())
    			.javaHome(getJavaHome().getValue())
    			.buildScansEnabled(getBuildScansEnabled().getValue())
    			.offlineMode(getOfflineMode().getValue())
    			.autoSync(getAutoSync().getValue())
                .arguments(getArguments().getValue())
                .jvmArguments(getJvmArguments().getValue())
                .showConsoleView(getShowConsoleView().getValue())
                .showExecutionsView(getShowExecutionsView().getValue())
                .projectAsCompositeRoot(getProjectAsCompositeRoot().getValue())
                .rootProject(getRootProject().getValue())
    			.build();
    }

}
