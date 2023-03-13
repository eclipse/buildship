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

import com.google.common.collect.ImmutableList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.ui.internal.preferences.GradleWorkbenchPreferencePage;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.GradleDistributionGroup.DistributionChangedListener;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;

/**
 * Page on the {@link ProjectImportWizard} declaring the used Gradle distribution and other advanced options for the imported project.
 */
public final class GradleOptionsWizardPage extends AbstractWizardPage {

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration) {
        this(configuration, ProjectWizardMessages.Title_GradleOptionsWizardPage, ProjectWizardMessages.InfoMessage_GradleOptionsWizardPageDefault);
    }

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration, String title, String defaultMessage) {
        super("GradleOptions", title, defaultMessage, configuration, ImmutableList.<Property<?>>of(configuration.getDistribution(), configuration.getGradleUserHome(), configuration.getJavaHome()));
    }

    @Override
    protected void createWidgets(Composite root) {
        GridLayoutFactory.swtDefaults().applyTo(root);
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.builder(root)
                .withOverrideCheckbox("Override workspace settings", "Configure Workspace Settings")
                .withAutoSyncCheckbox()
                .build();

        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.gradleProjectSettingsComposite);
        this.gradleProjectSettingsComposite.getParentPreferenceLink().addSelectionListener(new WorkbenchPreferenceOpeningSelectionListener());

        initValues();
        addListeners();
    }

    private void initValues() {
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().setSelection(getConfiguration().getOverrideWorkspaceConfiguration().getValue());
        this.gradleProjectSettingsComposite.getGradleDistributionGroup().setDistribution(getConfiguration().getDistribution().getValue());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setGradleUserHome(getConfiguration().getGradleUserHome().getValue());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJavaHome(getConfiguration().getJavaHome().getValue());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setArguments(getConfiguration().getArguments().getValue());
        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().setJvmArguments(getConfiguration().getJvmArguments().getValue());
        this.gradleProjectSettingsComposite.getBuildScansCheckbox().setSelection(getConfiguration().getBuildScansEnabled().getValue());
        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().setSelection(getConfiguration().getOfflineMode().getValue());
        this.gradleProjectSettingsComposite.getAutoSyncCheckbox().setSelection(getConfiguration().getAutoSync().getValue());
        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().setSelection(getConfiguration().getShowConsoleView().getValue());
        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().setSelection(getConfiguration().getShowExecutionsView().getValue());
        this.gradleProjectSettingsComposite.updateEnablement();
    }

    private void addListeners() {
        this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getOverrideWorkspaceConfiguration().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getOverrideWorkspaceConfiguration().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getOverrideBuildSettingsCheckbox().getSelection());
            }
        });

        this.gradleProjectSettingsComposite.getGradleDistributionGroup().addDistributionChangedListener(new DistributionChangedListener() {

            @Override
            public void distributionUpdated(GradleDistributionViewModel distribution) {
                getConfiguration().setDistribution(distribution);
            }
        });

        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHomeText().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setGradleUserHome(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getGradleUserHome());
            }
        });

        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHomeText().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConfiguration().setJavaHomeHome(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJavaHome());
            }
        });


        this.gradleProjectSettingsComposite.getBuildScansCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getBuildScansEnabled().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getBuildScansEnabled().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getBuildScansCheckbox().getSelection());
            }
        });

        this.gradleProjectSettingsComposite.getOfflineModeCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getOfflineMode().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getOfflineMode().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getOfflineModeCheckbox().getSelection());
            }
        });
        this.gradleProjectSettingsComposite.getAutoSyncCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getAutoSync().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAutoSyncCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getAutoSync().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAutoSyncCheckbox().getSelection());
            }
        });

        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArgumentsEditor().addChangeListener(() ->
                getConfiguration().getArguments().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getArguments()));

        this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArgumentsEditor().addChangeListener(() ->
                getConfiguration().getJvmArguments().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getAdvancedOptionsGroup().getJvmArguments()));

        this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getShowConsoleView().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getShowConsoleView().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getShowConsoleViewCheckbox().getSelection());
            }
        });

        this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConfiguration().getShowExecutionsView().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().getSelection());

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getConfiguration().getShowExecutionsView().setValue(GradleOptionsWizardPage.this.gradleProjectSettingsComposite.getShowExecutionsViewCheckbox().getSelection());
            }
        });

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
