/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.CoreTraceScopes;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.preferences.GradleProjectPreferencePage;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.GradleDistributionGroup.DistributionChangedListener;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;
import org.eclipse.buildship.ui.internal.util.widget.StringListEditor.StringListChangeListener;

/**
 * Specifies the Gradle distribution to apply when executing tasks via the run configurations.
 */
public final class ProjectSettingsTab extends AbstractLaunchConfigurationTab {

    private final Validator<GradleDistributionViewModel> distributionValidator;
    private final Validator<File> gradleUserHomeValidator;
    private final Validator<File> javaHomeValidator;

    private GradleRunConfigurationAttributes attributes;
    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    public ProjectSettingsTab() {
        this.distributionValidator = GradleDistributionViewModel.validator();
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Gradle_User_Home);
        this.javaHomeValidator = Validators.optionalDirectoryValidator(CoreMessages.Preference_Label_Java_Home);
    }

    @Override
    public String getName() {
        return LaunchMessages.Tab_Name_ProjectSettings;
    }

    @Override
    public Image getImage() {
        return PluginImages.RUN_CONFIG_GRADLE_DISTRIBUTION.withState(ImageState.ENABLED).getImage();
    }

    @Override
    public void createControl(Composite parent) {
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.builder(parent)
                .withOverrideCheckbox(CoreMessages.RunConfiguration_Label_OverrideProjectSettings, "Configure Project Settings")
                .showVariableSelector()
                .build();

        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this.gradleProjectSettingsComposite);
        setControl(this.gradleProjectSettingsComposite);
        addListeners();
    }

    private void addListeners() {
        DialogUpdater dialogUpdater = new DialogUpdater();
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().addDistributionChangedListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().addModifyListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().addModifyListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArgumentsEditor().addChangeListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArgumentsEditor().addChangeListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().addSelectionListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getParentPreferenceLink().addSelectionListener(new ProjectPreferenceOpeningSelectionListener());
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        this.attributes = GradleRunConfigurationAttributes.from(configuration);
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().setSelection(this.attributes.isOverrideBuildSettings());
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(GradleDistributionViewModel.from(this.attributes.getGradleDistribution()));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().setText(Strings.nullToEmpty(this.attributes.getGradleUserHomeHomeExpression()));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().setText(Strings.nullToEmpty(this.attributes.getJavaHomeExpression()));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setArguments(this.attributes.getArgumentExpressions());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJvmArguments(this.attributes.getJvmArgumentExpressions());
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(this.attributes.isOffline());
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(this.attributes.isBuildScansEnabled());
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().setSelection(this.attributes.isShowConsoleView());
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().setSelection(this.attributes.isShowExecutionView());
        this.gradleProjectSettingsComposite.updateEnablement();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyGradleDistribution(this.gradleProjectSettingsComposite.getGradleDistributionGroup().getDistribution().toGradleDistribution(), configuration);
        GradleRunConfigurationAttributes.applyGradleUserHomeExpression(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().getText(), configuration);
        GradleRunConfigurationAttributes.applyJavaHomeExpression(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().getText(), configuration);
        GradleRunConfigurationAttributes.applyArgumentExpressions(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArguments(), configuration);
        GradleRunConfigurationAttributes.applyJvmArgumentExpressions(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArguments(), configuration);
        GradleRunConfigurationAttributes.applyOfflineMode(this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyBuildScansEnabled(this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyShowConsoleView(this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyShowExecutionView(this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().getSelection(), configuration);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        GradleDistributionViewModel distribution = this.gradleProjectSettingsComposite.getGradleDistributionGroup().getDistribution();
        Optional<String> error = this.distributionValidator.validate(distribution);
        if (!error.isPresent()) {
            error = this.gradleUserHomeValidator.validate(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHome());
        }
        if (!error.isPresent()) {
            error = this.javaHomeValidator.validate(this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHome());
        }
        setErrorMessage(error.orNull());
        return !error.isPresent();
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // leave the controls empty
    }

    /**
     * Listener implementation to update the dialog buttons and messages.
     */
    private class DialogUpdater extends SelectionAdapter implements ModifyListener, DistributionChangedListener, StringListChangeListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void distributionUpdated(GradleDistributionViewModel distributionViewModel) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void onChange() {
            updateLaunchConfigurationDialog();
        }
    }

    /**
     * Opens the project preference dialog.
     */
    private class ProjectPreferenceOpeningSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        private void openWorkspacePreferences() {
            try {
                File workingDir = ProjectSettingsTab.this.attributes.getWorkingDir();
                Optional<IProject> project = CorePlugin.workspaceOperations().findProjectByLocation(workingDir);
                if (project.isPresent() && GradleProjectNature.isPresentOn(project.get())) {
                    PreferencesUtil.createPropertyDialogOn(getShell(), project.get(), GradleProjectPreferencePage.PAGE_ID, null, null).open();
                }
            } catch (Exception e) {
                CorePlugin.logger().trace(CoreTraceScopes.PREFERENCES, "Cannot open project preferences", e);
            }
        }
    }
}
