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

package org.eclipse.buildship.ui.launch;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.gradleware.tooling.toolingutils.binding.Validator;

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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.preferences.GradleProjectPreferencePage;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;
import org.eclipse.buildship.ui.util.widget.GradleProjectSettingsComposite;

/**
 * Specifies the Gradle distribution to apply when executing tasks via the run configurations.
 */
public final class ProjectSettingsTab extends AbstractLaunchConfigurationTab {

    private final Validator<GradleDistributionWrapper> gradleDistributionValidator;
    private final Validator<File> gradleUserHomeValidator;

    private GradleRunConfigurationAttributes attributes;
    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    public ProjectSettingsTab() {
        this.gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator("Gradle user home");
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
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.withOverrideCheckbox(parent, CoreMessages.RunConfiguration_Label_OverrideProjectSettings, "Configure Project Settings");
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
        this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().addModifyListener(dialogUpdater);
        this.gradleProjectSettingsComposite.getParentPreferenceLink().addSelectionListener(new ProjectPreferenceOpeningSelectionListener());
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        this.attributes = GradleRunConfigurationAttributes.from(configuration);
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().setSelection(this.attributes.isOverrideBuildSettings());
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setGradleDistribution(GradleDistributionWrapper.from(this.attributes.getGradleDistribution()));
        this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().setText(Strings.nullToEmpty(this.attributes.getGradleUserHomeHomeExpression()));
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(this.attributes.isOffline());
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(this.attributes.isBuildScansEnabled());
        this.gradleProjectSettingsComposite.updateEnablement();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyGradleDistribution(this.gradleProjectSettingsComposite.getGradleDistributionGroup().getGradleDistribution().toGradleDistribution(), configuration);
        GradleRunConfigurationAttributes.applyGradleUserHomeExpression(this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHomeText().getText(), configuration);
        GradleRunConfigurationAttributes.applyOfflineMode(this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection(), configuration);
        GradleRunConfigurationAttributes.applyBuildScansEnabled(this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection(), configuration);

    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        GradleDistributionWrapper gradleDistribution = this.gradleProjectSettingsComposite.getGradleDistributionGroup().getGradleDistribution();
        Optional<String> error = this.gradleDistributionValidator.validate(gradleDistribution);
        if (!error.isPresent()) {
            error = this.gradleUserHomeValidator.validate(this.gradleProjectSettingsComposite.getGradleUserHomeGroup().getGradleUserHome());
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
    private class DialogUpdater extends SelectionAdapter implements ModifyListener, DistributionChangedListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void distributionUpdated(GradleDistributionWrapper distribution) {
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
                CorePlugin.logger().debug("Cannot open project preferences", e);
            }
        }
    }
}
