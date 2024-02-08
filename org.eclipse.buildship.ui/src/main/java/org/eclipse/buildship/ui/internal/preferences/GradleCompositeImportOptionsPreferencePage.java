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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.CompositeConfiguration;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.configuration.DefaultCompositeConfiguration;
import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.AdvancedOptionsGroup;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Preference page for composite import options.
 *
 * @author Sebastian Kuzniarz
 */

public final class GradleCompositeImportOptionsPreferencePage extends PropertyPage implements IWorkbenchPropertyPage{

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
        this.gradleProjectSettingsComposite.setVisible(true);
        
        initValues();
        addListeners();
        return this.gradleProjectSettingsComposite;
    }

    @Override
    public void applyData(Object data) {
    	// TODO Auto-generated method stub
    	super.applyData(data);
    	
    }
    
    private void initValues() {
        IWorkingSet composite = getTargetComposite();
        
        BuildConfiguration buildConfig = CorePlugin.configurationManager().loadCompositeConfiguration(composite.getName()).getBuildConfiguration();
        
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
        IWorkingSet composite = getTargetComposite();
        List<File> compositeBuilds = getIncludedBuildsList(composite);
        ConfigurationManager manager = CorePlugin.configurationManager();
        CompositeConfiguration currentConfig = manager.loadCompositeConfiguration(composite.getName());
        
        BuildConfiguration updatedConfig = manager.createBuildConfiguration(currentConfig.getBuildConfiguration().getRootProjectDirectory(),
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
        CompositeConfiguration compConf = new DefaultCompositeConfiguration(composite.getName(), compositeBuilds, updatedConfig, currentConfig.projectAsCompositeRoot());
        manager.saveCompositeConfiguration(compConf); 
        return true;
    }

    private List<File> getIncludedBuildsList(IWorkingSet composite) {
    	List<File> includedBuildsList = new ArrayList<File>();
    	InternalGradleBuild gradleBuild = null;
		for (IAdaptable element : composite.getElements()) {
			if (CorePlugin.internalGradleWorkspace().getBuild(((IProject) element)).isPresent()) {
				gradleBuild = (InternalGradleBuild) CorePlugin.internalGradleWorkspace().getBuild(((IProject) element)).get();
				includedBuildsList.add(gradleBuild.getBuildConfig().getRootProjectDirectory());
			} 
		}
		return includedBuildsList;
	}

	@SuppressWarnings("cast")
    private IWorkingSet getTargetComposite() {
        return (IWorkingSet) Platform.getAdapterManager().getAdapter(getElement(), IWorkingSet.class);
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
