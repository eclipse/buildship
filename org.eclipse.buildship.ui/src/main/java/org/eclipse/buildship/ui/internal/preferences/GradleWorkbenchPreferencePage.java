/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.preferences;

import java.io.File;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.AdvancedOptionsGroup;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;

/**
 * The main workspace preference page for Buildship. Currently only used to configure the Gradle
 * User Home.
 */
public final class GradleWorkbenchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.preferences";

    private final Validator<File> gradleUserHomeValidator;
    private final Validator<File> javaHomeValidator;
    private final Validator<GradleDistributionViewModel> distributionValidator;

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;
    private boolean experimentalModuleSupportEnabled;


    public GradleWorkbenchPreferencePage() {
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Gradle_User_Home);
        this.javaHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Java_Home);
        this.distributionValidator = GradleDistributionViewModel.validator();
    }

    @Override
    protected Control createContents(Composite parent) {
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.builder(parent)
                .withAutoSyncCheckbox()
                .build();

        initValues();
        addListeners();

        return this.gradleProjectSettingsComposite;
    }

    private void initValues() {
        WorkspaceConfiguration config = CorePlugin.configurationManager().loadWorkspaceConfiguration();
        GradleDistribution gradleDistribution = config.getGradleDistribution();
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(GradleDistributionViewModel.from(gradleDistribution));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setGradleUserHome(config.getGradleUserHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJavaHome(config.getJavaHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setArguments(config.getArguments());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJvmArguments(config.getJvmArguments());
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(config.isOffline());
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(config.isBuildScansEnabled());
        this.gradleProjectSettingsComposite.getAutoSyncCheckbox().setSelection(config.isAutoSync());
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().setSelection(config.isShowConsoleView());
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().setSelection(config.isShowExecutionsView());
        this.experimentalModuleSupportEnabled = config.isExperimentalModuleSupportEnabled();
    }

    private void addListeners() {
        AdvancedOptionsGroup advancedOptionsGroup = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup();
        advancedOptionsGroup.getGradleUserHomeText().addModifyListener(new ValidatingListener<>(this, () -> advancedOptionsGroup.getGradleUserHome(), this.gradleUserHomeValidator));
        advancedOptionsGroup.getJavaHomeText().addModifyListener(new ValidatingListener<>(this, () -> advancedOptionsGroup.getJavaHome(), this.javaHomeValidator));
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().addDistributionChangedListener(new GradleDistributionValidatingListener(this, this.distributionValidator));
    }

    @Override
    public boolean performOk() {
        GradleDistribution distribution = this.gradleProjectSettingsComposite.getGradleDistributionGroup().getDistribution().toGradleDistribution();
        String gradleUserHomeString = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().getText();
        File gradleUserHome = gradleUserHomeString.isEmpty() ? null : new File(gradleUserHomeString);
        String javaHomeString = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().getText();
        File javaHome = javaHomeString.isEmpty() ? null : new File(javaHomeString);
        boolean offlineMode = this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection();
        boolean buildScansEnabled = this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection();
        boolean autoSync = this.gradleProjectSettingsComposite.getAutoSyncCheckbox().getSelection();
        List<String> arguments = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArguments();
        List<String> jvmArguments = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArguments();
        boolean showConsoleView = this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().getSelection();
        boolean showExecutionsView = this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().getSelection();
        WorkspaceConfiguration workspaceConfig = new WorkspaceConfiguration(distribution, gradleUserHome, javaHome, offlineMode, buildScansEnabled, autoSync, arguments, jvmArguments, showConsoleView, showExecutionsView, this.experimentalModuleSupportEnabled);
        CorePlugin.configurationManager().saveWorkspaceConfiguration(workspaceConfig);
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().setText("");
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().setText("");
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(GradleDistributionViewModel.from(GradleDistribution.fromBuild()));
        super.performDefaults();
    }

    @Override
    public void init(IWorkbench workbench) {
    }
}
