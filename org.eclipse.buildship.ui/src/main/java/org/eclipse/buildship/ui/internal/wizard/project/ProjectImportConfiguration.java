/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;

/**
 * Serves as the data model of the project import wizard.
 */
public final class ProjectImportConfiguration {

    private final Property<File> projectDir;
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

    public ProjectImportConfiguration() {
        this(Validators.<File>noOp(), Validators.<GradleDistributionViewModel>noOp(), Validators.<File>noOp(), Validators.<File>noOp(), Validators.<Boolean>noOp(), Validators.<List<String>>noOp());
    }

    public ProjectImportConfiguration(Validator<File> projectDirValidator, Validator<GradleDistributionViewModel> distributionValidator,
            Validator<File> gradleUserHomeValidator, Validator<File> javaHomeValidator, Validator<Boolean> applyWorkingSetsValidator, Validator<List<String>> workingSetsValidators) {
        this.projectDir = Property.create(projectDirValidator);
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
    }

    public Property<File> getProjectDir() {
        return this.projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir.setValue(projectDir);
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

    public BuildConfiguration toInternalBuildConfiguration() {
        return CorePlugin.configurationManager().createBuildConfiguration(getProjectDir().getValue(),
                getOverrideWorkspaceConfiguration().getValue(),
                getDistribution().getValue().toGradleDistribution(),
                getGradleUserHome().getValue(),
                getJavaHome().getValue(),
                getBuildScansEnabled().getValue(),
                getOfflineMode().getValue(),
                getAutoSync().getValue(),
                getArguments().getValue(),
                getJvmArguments().getValue(),
                getShowConsoleView().getValue(),
                getShowExecutionsView().getValue());
    }

    public org.eclipse.buildship.core.BuildConfiguration toBuildConfiguration() {
        return org.eclipse.buildship.core.BuildConfiguration.forRootProjectDirectory(getProjectDir().getValue())
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
                .build();
    }
}
