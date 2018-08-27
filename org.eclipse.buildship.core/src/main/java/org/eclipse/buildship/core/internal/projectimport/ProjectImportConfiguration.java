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

package org.eclipse.buildship.core.internal.projectimport;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistributionInfo;

/**
 * Serves as the data model of the project import wizard.
 */
public final class ProjectImportConfiguration {

    private final Property<File> projectDir;
    private final Property<Boolean> overwriteWorkspaceSettings;
    private final Property<GradleDistributionInfo> distributionInfo;
    private final Property<File> gradleUserHome;
    private final Property<Boolean> applyWorkingSets;
    private final Property<List<String>> workingSets;
    private final Property<Boolean> buildScansEnabled;
    private final Property<Boolean> offlineMode;
    private final Property<Boolean> autoSync;

    public ProjectImportConfiguration() {
        this(Validators.<File>noOp(), Validators.<GradleDistributionInfo>noOp(), Validators.<File>noOp(), Validators.<Boolean>noOp(), Validators.<List<String>>noOp());
    }

    public ProjectImportConfiguration(Validator<File> projectDirValidator, Validator<GradleDistributionInfo> distributionInfoValidator,
            Validator<File> gradleUserHomeValidator, Validator<Boolean> applyWorkingSetsValidator, Validator<List<String>> workingSetsValidators) {
        this.projectDir = Property.create(projectDirValidator);
        this.overwriteWorkspaceSettings = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.distributionInfo = Property.create(distributionInfoValidator);
        this.gradleUserHome = Property.create(gradleUserHomeValidator);
        this.applyWorkingSets = Property.create(applyWorkingSetsValidator);
        this.workingSets = Property.create(workingSetsValidators);
        this.buildScansEnabled = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.offlineMode = Property.<Boolean>create(Validators.<Boolean>noOp());
        this.autoSync = Property.<Boolean>create(Validators.<Boolean>noOp());
    }

    public Property<File> getProjectDir() {
        return this.projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir.setValue(projectDir);
    }


    public Property<Boolean> getOverwriteWorkspaceSettings() {
        return this.overwriteWorkspaceSettings;
    }

    public void setOverwriteWorkspaceSettings(boolean overwriteWorkspaceSettings) {
        this.overwriteWorkspaceSettings.setValue(Boolean.valueOf(overwriteWorkspaceSettings));
    }

    public Property<GradleDistributionInfo> getDistributionInfo() {
        return this.distributionInfo;
    }

    public void setDistributionInfo(GradleDistributionInfo distributionInfo) {
        this.distributionInfo.setValue(distributionInfo);
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

    public BuildConfiguration toBuildConfig() {
        return CorePlugin.configurationManager().createBuildConfiguration(getProjectDir().getValue(),
                getOverwriteWorkspaceSettings().getValue(),
                getDistributionInfo().getValue().toGradleDistribution(),
                getGradleUserHome().getValue(),
                getBuildScansEnabled().getValue(),
                getOfflineMode().getValue(),
                getAutoSync().getValue());
    }
}
