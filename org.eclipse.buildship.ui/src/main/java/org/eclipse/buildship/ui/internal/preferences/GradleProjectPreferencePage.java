/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.preferences;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.AdvancedOptionsGroup;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;

/**
 * Preference page for Gradle projects.
 *
 * @author Donat Csikos
 */
public final class GradleProjectPreferencePage extends PropertyPage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.projectproperties";

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    private final Validator<GradleDistributionViewModel> distributionValidator;
    private final Validator<File> javaHomeValidator;
    private final Validator<File> gradleUserHomeValidator;

    public GradleProjectPreferencePage() {
        this.gradleUserHomeValidator = Validators.optionalDirectoryValidator("Gradle user home");
        this.javaHomeValidator = Validators.optionalDirectoryValidator("Java home");
        this.distributionValidator = GradleDistributionViewModel.validator();
    }

    @Override
    protected Control createContents(Composite parent) {
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.builder(parent)
                .withAutoSyncCheckbox()
                .withOverrideCheckbox("Override workspace settings", "Configure Workspace Settings")
                .build();

        initValues();
        addListeners();

        return this.gradleProjectSettingsComposite;
    }

    private void initValues() {
        IProject project = getTargetProject();
        BuildConfiguration buildConfig = CorePlugin.configurationManager().loadProjectConfiguration(project).getBuildConfiguration();
        boolean overrideWorkspaceSettings = buildConfig.isOverrideWorkspaceSettings();
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(GradleDistributionViewModel.from(buildConfig.getProperties().getGradleDistribution()));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setGradleUserHome(buildConfig.getProperties().getGradleUserHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJavaHome(buildConfig.getProperties().getJavaHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setArguments(buildConfig.getProperties().getArguments());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJvmArguments(buildConfig.getProperties().getJvmArguments());
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().setSelection(overrideWorkspaceSettings);
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(buildConfig.getProperties().isBuildScansEnabled());
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(buildConfig.getProperties().isOfflineMode());
        this.gradleProjectSettingsComposite.getAutoSyncCheckbox().setSelection(buildConfig.getProperties().isAutoSync());
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().setSelection(buildConfig.getProperties().isShowConsoleView());
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().setSelection(buildConfig.getProperties().isShowExecutionsView());
        this.gradleProjectSettingsComposite.updateEnablement();
    }

    private void addListeners() {
        this.gradleProjectSettingsComposite.getParentPreferenceLink().addSelectionListener(new WorkbenchPreferenceOpeningSelectionListener());
        AdvancedOptionsGroup advancedOptionsGroup = this.gradleProjectSettingsComposite.getAdvancedOptionsGroup();
        advancedOptionsGroup.getGradleUserHomeText().addModifyListener(new ValidatingListener<>(this, () -> advancedOptionsGroup.getGradleUserHome(), this.gradleUserHomeValidator));
        advancedOptionsGroup.getJavaHomeText().addModifyListener(new ValidatingListener<>(this, () -> advancedOptionsGroup.getJavaHome(), this.javaHomeValidator));
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().addDistributionChangedListener(new GradleDistributionValidatingListener(this, this.distributionValidator));
    }

    @Override
    public boolean performOk() {
       IProject project = getTargetProject();
       ConfigurationManager manager = CorePlugin.configurationManager();
       BuildConfiguration currentConfig = manager.loadProjectConfiguration(project).getBuildConfiguration();
       BuildConfiguration updatedConfig = manager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
           this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().getSelection(),
           this.gradleProjectSettingsComposite.getGradleDistributionGroup().getDistribution().toGradleDistribution(),
           this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHome(),
           this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHome(),
           this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection(),
           this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection(),
           this.gradleProjectSettingsComposite.getAutoSyncCheckbox().getSelection(),
           this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArguments(),
           this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArguments(),
           this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().getSelection(),
           this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().getSelection());
       manager.saveBuildConfiguration(updatedConfig);
       return true;
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private IProject getTargetProject() {
        return (IProject) Platform.getAdapterManager().getAdapter(getElement(), IProject.class);
    }

    /**
     * Opens the workspace preferences dialog.
     */
    private class WorkbenchPreferenceOpeningSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        private void openWorkspacePreferences() {
            PreferencesUtil.createPreferenceDialogOn(getShell(), GradleWorkbenchPreferencePage.PAGE_ID, null, null).open();
        }
    }
}
