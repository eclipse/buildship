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

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.util.selection.Enabler;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Specifies the Gradle distribution to apply when executing tasks via the run configurations.
 */
public final class ProjectSettingsTab extends AbstractLaunchConfigurationTab {

    private final Validator<GradleDistributionWrapper> gradleDistributionValidator;
    private final PublishedGradleVersionsWrapper publishedGradleVersions;

    private Button overrideBuildSettingsCheckbox;
    private Button offlineModeCheckbox;
    private Button buildScansEnabledCheckbox;
    private GradleDistributionGroup gradleDistributionGroup;
    private Enabler overrideProjectSettingsEnabler;

    public ProjectSettingsTab() {
        this.gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
        this.publishedGradleVersions = CorePlugin.publishedGradleVersions();
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
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);
        setControl(page);

        this.overrideBuildSettingsCheckbox = new Button(page, SWT.CHECK);
        this.overrideBuildSettingsCheckbox.setText(CoreMessages.RunConfiguration_Label_OverrideProjectSettings);

        Group buildExecutionGroup = new Group(page, SWT.NONE);
        buildExecutionGroup.setText(CoreMessages.RunConfiguration_Label_BuildExecution + ":");
        buildExecutionGroup.setLayout(new GridLayout(3, false));
        buildExecutionGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        this.offlineModeCheckbox = new Button(buildExecutionGroup, SWT.CHECK);
        this.offlineModeCheckbox.setText("Offline Mode");
        this.buildScansEnabledCheckbox = new Button(buildExecutionGroup, SWT.CHECK);
        this.buildScansEnabledCheckbox.setText("Build Scans");

        this.gradleDistributionGroup = new GradleDistributionGroup(this.publishedGradleVersions, page);

        setupEnablement();
        addListeners();
    }

    private void setupEnablement() {
        this.overrideProjectSettingsEnabler = new Enabler(this.overrideBuildSettingsCheckbox).enables(this.offlineModeCheckbox, this.buildScansEnabledCheckbox, this.gradleDistributionGroup);
    }

    private void addListeners() {
        DialogUpdater dialogUpdater = new DialogUpdater();
        this.overrideBuildSettingsCheckbox.addSelectionListener(dialogUpdater);
        this.offlineModeCheckbox.addSelectionListener(dialogUpdater);
        this.buildScansEnabledCheckbox.addSelectionListener(dialogUpdater);
        this.gradleDistributionGroup.addDistributionChangedListener(dialogUpdater);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(configuration);
        this.gradleDistributionGroup.setGradleDistribution(GradleDistributionWrapper.from(configurationAttributes.getGradleDistribution()));
        this.overrideBuildSettingsCheckbox.setSelection(configurationAttributes.isOverrideBuildSettings());
        this.offlineModeCheckbox.setSelection(configurationAttributes.isOffline());
        this.buildScansEnabledCheckbox.setSelection(configurationAttributes.isBuildScansEnabled());
        this.overrideProjectSettingsEnabler.updateEnablement();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyGradleDistribution(this.gradleDistributionGroup.getGradleDistribution().toGradleDistribution(), configuration);
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(this.overrideBuildSettingsCheckbox.getSelection(), configuration);
        GradleRunConfigurationAttributes.applyOfflineMode(this.offlineModeCheckbox.getSelection(), configuration);
        GradleRunConfigurationAttributes.applyBuildScansEnabled(this.buildScansEnabledCheckbox.getSelection(), configuration);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        GradleDistributionWrapper gradleDistribution = this.gradleDistributionGroup.getGradleDistribution();
        Optional<String> error = this.gradleDistributionValidator.validate(gradleDistribution);
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
}
