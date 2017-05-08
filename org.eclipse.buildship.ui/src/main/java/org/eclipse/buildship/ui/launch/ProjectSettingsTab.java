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

import java.util.List;

import org.gradle.util.GradleVersion;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.gradle.GradleDistributionValidator;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.selection.Enabler;
import org.eclipse.buildship.ui.util.widget.ButtonUtils;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * Specifies the Gradle distribution to apply when executing tasks via the run configurations.
 */
public final class ProjectSettingsTab extends AbstractLaunchConfigurationTab {

    private final Font defaultFont;
    private final UiBuilder.UiBuilderFactory builderFactory;
    private final Validator<GradleDistributionWrapper> gradleDistributionValidator;
    private final PublishedGradleVersionsWrapper publishedGradleVersions;

    private Text localInstallationDirText;
    private Text remoteDistributionUriText;
    private Combo gradleVersionCombo;

    private Button useGradleWrapperOption;
    private Button useLocalInstallationDirOption;
    private Button localInstallationDirBrowseButton;
    private Button useRemoteDistributionUriOption;
    private Button useGradleVersionOption;
    private List<Button> allRadioButtons;
    private Button overrideBuildSettingsCheckbox;
    private Button offlineModeCheckbox;
    private Button buildScansEnabledCheckbox;
    private Enabler overrideProjectSettingsEnabler;

    private final SelectionListener optionSelectionChangedListener;
    private final ModifyListener textChangedListener;

    // if different configurations are loaded we change radio buttons and text fields. this
    // generates several change events which trigger the launch config to update and leads to
    // an inconsistent state. to resolve that, we disable the launch config update in when the
    // initializeFrom() method is called
    private boolean disableUpdateDialog = false;

    public ProjectSettingsTab() {
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);
        this.gradleDistributionValidator = GradleDistributionValidator.gradleDistributionValidator();
        this.publishedGradleVersions = CorePlugin.publishedGradleVersions();

        this.optionSelectionChangedListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!ProjectSettingsTab.this.disableUpdateDialog) {
                    updateLaunchConfigurationDialog();
                }
            }
        };
        this.textChangedListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (!ProjectSettingsTab.this.disableUpdateDialog) {
                    updateLaunchConfigurationDialog();
                }
            }
        };
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
        this.overrideBuildSettingsCheckbox.setText("Override workspace settings");

        Group overridePreferencesGroup = createGroup(page, CoreMessages.RunConfiguration_Label_BuildExecution + ":");
        createOverrideProjectPreferencesControl(overridePreferencesGroup);

        Group gradleDistributionGroup = createGroup(page, CoreMessages.RunConfiguration_Label_GradleDistribution + ":");
        createGradleDistributionSelectionControl(gradleDistributionGroup);

        this.overrideProjectSettingsEnabler = new Enabler(this.overrideBuildSettingsCheckbox);
        this.overrideProjectSettingsEnabler.enables(this.offlineModeCheckbox,
                this.buildScansEnabledCheckbox,
                this.useGradleWrapperOption,
                this.useLocalInstallationDirOption,
                this.localInstallationDirText,
                this.localInstallationDirBrowseButton,
                this.useRemoteDistributionUriOption,
                this.remoteDistributionUriText,
                this.useGradleVersionOption,
                this.gradleVersionCombo);
    }

    private Group createGroup(Composite parent, String groupName) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(groupName);
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private void createGradleDistributionSelectionControl(Group container) {
        // gradle wrapper
        this.useGradleWrapperOption = this.builderFactory.newRadio(container).alignLeft().text(CoreMessages.GradleDistribution_Label_GradleWrapper).control();
        this.builderFactory.span(container);
        this.builderFactory.span(container);

        // local installation directory
        this.useLocalInstallationDirOption = this.builderFactory.newRadio(container).alignLeft().text(CoreMessages.GradleDistribution_Label_LocalInstallationDirectory).control();
        this.localInstallationDirText = this.builderFactory.newText(container).alignFillHorizontal().disabled().control();
        this.localInstallationDirBrowseButton = this.builderFactory.newButton(container).alignLeft().disabled().text(UiMessages.Button_Label_Browse).control();
        this.localInstallationDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(container.getShell(), this.localInstallationDirText,
                CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));

        // remote distribution installation
        this.useRemoteDistributionUriOption = this.builderFactory.newRadio(container).alignLeft().text(CoreMessages.GradleDistribution_Label_RemoteDistributionUri).control();
        this.remoteDistributionUriText = this.builderFactory.newText(container).alignFillHorizontal().disabled().control();
        this.builderFactory.span(container);

        // gradle version
        this.useGradleVersionOption = this.builderFactory.newRadio(container).alignLeft().text(CoreMessages.GradleDistribution_Label_SpecificGradleVersion).control();
        this.gradleVersionCombo = this.builderFactory.newCombo(container).alignLeft().disabled().control();
        this.gradleVersionCombo.setSize(150, this.gradleVersionCombo.getSize().y);
        this.gradleVersionCombo.setItems(getGradleVersions());
        this.builderFactory.span(container);

        // update launch configuration when the content of the widgets are changing
        this.localInstallationDirText.addModifyListener(this.textChangedListener);
        this.remoteDistributionUriText.addModifyListener(this.textChangedListener);
        this.gradleVersionCombo.addModifyListener(this.textChangedListener);

        // update the enabled/disabled state when the currently selected radio is changing
        this.useGradleWrapperOption.addSelectionListener(this.optionSelectionChangedListener);
        this.useLocalInstallationDirOption.addSelectionListener(this.optionSelectionChangedListener);
        this.useRemoteDistributionUriOption.addSelectionListener(this.optionSelectionChangedListener);
        this.useGradleVersionOption.addSelectionListener(this.optionSelectionChangedListener);

        // update the enabled/disabled state when the currently selected radio is changing
        new Enabler(this.useGradleWrapperOption).enables();
        new Enabler(this.useLocalInstallationDirOption).enables(this.localInstallationDirText, this.localInstallationDirBrowseButton);
        new Enabler(this.useRemoteDistributionUriOption).enables(this.remoteDistributionUriText);
        new Enabler(this.useGradleVersionOption).enables(this.gradleVersionCombo);

        // save all created radio buttons in a list
        this.allRadioButtons = ImmutableList.of(this.useGradleWrapperOption, this.useLocalInstallationDirOption, this.useRemoteDistributionUriOption, this.useGradleVersionOption);
    }

    private void createOverrideProjectPreferencesControl(Composite container) {
        GridLayout layout = new GridLayout(3, false);
        container.setLayout(layout);

        this.offlineModeCheckbox = new Button(container, SWT.CHECK);
        this.offlineModeCheckbox.setText("Offline Mode");
        this.buildScansEnabledCheckbox = new Button(container, SWT.CHECK);
        this.buildScansEnabledCheckbox.setText("Build Scans");

        this.overrideBuildSettingsCheckbox.addSelectionListener(new DialogUpdater());
        this.offlineModeCheckbox.addSelectionListener(new DialogUpdater());
        this.buildScansEnabledCheckbox.addSelectionListener(new DialogUpdater());
    }

    private String[] getGradleVersions() {
        return FluentIterable.from(this.publishedGradleVersions.getVersions()).transform(new Function<GradleVersion, String>() {

            @Override
            public String apply(GradleVersion gradleVersion) {
                return gradleVersion.getVersion();
            }
        }).toArray(String.class);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        // do not trigger save configuration (through events) when initializing the values
        this.disableUpdateDialog = true;
        try {
            GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(configuration);
            setSelection(GradleDistributionWrapper.from(configurationAttributes.getGradleDistribution()));

            this.overrideBuildSettingsCheckbox.setSelection(configurationAttributes.isOverrideBuildSettings());
            this.offlineModeCheckbox.setSelection(configurationAttributes.isOffline());
            this.buildScansEnabledCheckbox.setSelection(configurationAttributes.isBuildScansEnabled());

            this.overrideProjectSettingsEnabler.updateEnablement();
        } finally {
            this.disableUpdateDialog = false;
        }
    }

    private void resetValues() {
        this.localInstallationDirText.setText("");
        this.remoteDistributionUriText.setText("");
        if (this.gradleVersionCombo.getItemCount() > 0) {
            this.gradleVersionCombo.select(0);
        } else {
            this.gradleVersionCombo.setText("");
        }
    }

    private void setSelection(GradleDistributionWrapper wrapper) {
        resetValues();
        // set correct enabled state and specific configuration values
        DistributionType type = wrapper.getType();
        String configuration = Strings.nullToEmpty(wrapper.getConfiguration());
        switch (type) {
            case WRAPPER:
                updateSelectedState(this.useGradleWrapperOption, this.allRadioButtons);
                break;
            case LOCAL_INSTALLATION:
                updateSelectedState(this.useLocalInstallationDirOption, this.allRadioButtons);
                this.localInstallationDirText.setText(configuration);
                break;
            case REMOTE_DISTRIBUTION:
                updateSelectedState(this.useRemoteDistributionUriOption, this.allRadioButtons);
                this.remoteDistributionUriText.setText(configuration);
                break;
            case VERSION:
                updateSelectedState(this.useGradleVersionOption, this.allRadioButtons);
                updateVersionSelection(configuration);
                break;
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + type); //$NON-NLS-1$
        }
    }

    private void updateSelectedState(Button selectedRadioButton, List<Button> allRadioButtons) {
        for (Button radioButton : allRadioButtons) {
            boolean selected = radioButton.equals(selectedRadioButton);
            ButtonUtils.setSelectionAndFireEvent(radioButton, selected);
            if (selected) {
                radioButton.setFocus();
            }
        }
    }

    private void updateVersionSelection(String version) {
        // select the version in the combo box if the exist
        for (int i = 0; i < this.gradleVersionCombo.getItemCount(); ++i) {
            if (this.gradleVersionCombo.getItem(i).equals(version)) {
                this.gradleVersionCombo.select(i);
                return;
            }
        }

        // otherwise, create a new entry in the combo and select it
        int index = this.gradleVersionCombo.getItemCount();
        this.gradleVersionCombo.add(version, index);
        this.gradleVersionCombo.select(index);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        GradleRunConfigurationAttributes.applyGradleDistribution(getSelection().toGradleDistribution(), configuration);
        GradleRunConfigurationAttributes.applyOverrideBuildSettings(this.overrideBuildSettingsCheckbox.getSelection(), configuration);
        GradleRunConfigurationAttributes.applyOfflineMode(this.offlineModeCheckbox.getSelection(), configuration);
        GradleRunConfigurationAttributes.applyBuildScansEnabled(this.buildScansEnabledCheckbox.getSelection(), configuration);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        GradleDistributionWrapper gradleDistribution = getSelection();
        Optional<String> error = this.gradleDistributionValidator.validate(gradleDistribution);
        setErrorMessage(error.orNull());
        return !error.isPresent();
    }

    private GradleDistributionWrapper getSelection() {
        if (this.useGradleWrapperOption.getSelection()) {
            return GradleDistributionWrapper.from(DistributionType.WRAPPER, null);
        } else if (this.useLocalInstallationDirOption.getSelection()) {
            return GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION, this.localInstallationDirText.getText());
        } else if (this.useRemoteDistributionUriOption.getSelection()) {
            return GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION, this.remoteDistributionUriText.getText());
        } else if (this.useGradleVersionOption.getSelection()) {
            return getGradleDistributionWrapper();
        } else {
            throw new IllegalStateException("No Gradle distribution selected.");
        }
    }

    private GradleDistributionWrapper getGradleDistributionWrapper() {
        int selectionIndex = this.gradleVersionCombo.getSelectionIndex();
        return GradleDistributionWrapper.from(DistributionType.VERSION,
                Strings.emptyToNull(selectionIndex == -1 || !Strings.isNullOrEmpty(this.gradleVersionCombo.getText()) ?
                        this.gradleVersionCombo.getText() :
                        this.gradleVersionCombo.getItem(selectionIndex)));
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // leave the controls empty
    }

    @Override
    public void dispose() {
        this.defaultFont.dispose();
        super.dispose();
    }

    /**
     * Listener implementation to update the dialog buttons and messages.
     */
    private class DialogUpdater extends SelectionAdapter implements ModifyListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }
    }

}
