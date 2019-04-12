/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.preferences;

import java.io.File;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.AdvancedOptionsGroup;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Preference page for composite import options.
 *
 * @author Sebastian Kuzniarz
 */
public final class GradleCompositeImportOptionsPreferencePage extends PropertyPage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.compositeImportOptionsProperties";

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    private final Validator<GradleDistributionViewModel> distributionValidator;
    private final Validator<File> javaHomeValidator;
    private final Validator<File> gradleUserHomeValidator;

    public GradleCompositeImportOptionsPreferencePage() {
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
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(GradleDistributionViewModel.from(buildConfig.getGradleDistribution()));
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setGradleUserHome(buildConfig.getGradleUserHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJavaHome(buildConfig.getJavaHome());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setArguments(buildConfig.getArguments());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJvmArguments(buildConfig.getJvmArguments());
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().setSelection(overrideWorkspaceSettings);
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(buildConfig.isBuildScansEnabled());
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(buildConfig.isOfflineMode());
        this.gradleProjectSettingsComposite.getAutoSyncCheckbox().setSelection(buildConfig.isAutoSync());
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().setSelection(buildConfig.isShowConsoleView());
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().setSelection(buildConfig.isShowExecutionsView());
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

    @SuppressWarnings("cast")
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
