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

package org.eclipse.buildship.ui.wizard.project;

import java.util.List;

import org.gradle.util.GradleVersion;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.util.selection.Enabler;
import org.eclipse.buildship.ui.util.widget.UiBuilder;

/**
 * Page on the {@link ProjectImportWizard} declaring the used Gradle distribution and other advanced options for the imported project.
 */
public final class GradleOptionsWizardPage extends AbstractWizardPage {

    private final PublishedGradleVersionsWrapper publishedGradleVersions;
    private final String pageContextInformation;

    // widgets in the Gradle distribution group
    private Text localInstallationDirText;
    private Text remoteDistributionUriText;
    private Combo gradleVersionCombo;
    private Button useGradleWrapperOption;
    private Button useLocalInstallationDirOption;
    private Button useRemoteDistributionUriOption;
    private Button useGradleVersionOption;

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration, PublishedGradleVersionsWrapper publishedGradleVersions) {
        this(configuration, publishedGradleVersions, ProjectWizardMessages.Title_GradleOptionsWizardPage, ProjectWizardMessages.InfoMessage_GradleOptionsWizardPageDefault,
                ProjectWizardMessages.InfoMessage_GradleOptionsWizardPageContext);
    }

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration, PublishedGradleVersionsWrapper publishedGradleVersions, String title, String defaultMessage, String pageContextInformation) {
        super("GradleOptions", title, defaultMessage, configuration, ImmutableList.<Property<?>>of(configuration.getGradleDistribution()));
        this.publishedGradleVersions = publishedGradleVersions;
        this.pageContextInformation = pageContextInformation;
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(new GridLayout(1, false));
        createGradleDistributionContent(createGroup(root, ProjectWizardMessages.Label_GradleDistribution));
        bindGradleDistributionToConfiguration();
    }

    private static Group createGroup(Composite parent, String text) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(text);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setLayout(LayoutUtils.newGridLayout(3));
        return group;
    }

    private void createGradleDistributionContent(Composite root) {
        UiBuilder.UiBuilderFactory uiBuilderFactory = getUiBuilderFactory();

        // first line: gradle wrapper
        this.useGradleWrapperOption = uiBuilderFactory.newRadio(root).alignLeft().text(CoreMessages.GradleDistribution_Label_GradleWrapper).control();

        uiBuilderFactory.span(root);
        uiBuilderFactory.span(root);

        // second line: local installation directory
        this.useLocalInstallationDirOption = uiBuilderFactory.newRadio(root).alignLeft().text(CoreMessages.GradleDistribution_Label_LocalInstallationDirectory).control();
        this.localInstallationDirText = uiBuilderFactory.newText(root).alignFillHorizontal().disabled().control();
        Button localInstallationDirBrowseButton = uiBuilderFactory.newButton(root).alignLeft().disabled().text(UiMessages.Button_Label_Browse).control();
        localInstallationDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(root.getShell(), this.localInstallationDirText, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));

        // third line: remote distribution installation
        this.useRemoteDistributionUriOption = uiBuilderFactory.newRadio(root).alignLeft().text(CoreMessages.GradleDistribution_Label_RemoteDistributionUri).control();
        this.remoteDistributionUriText = uiBuilderFactory.newText(root).alignFillHorizontal().disabled().control();
        uiBuilderFactory.span(root);

        // fourth line: gradle version
        this.useGradleVersionOption = uiBuilderFactory.newRadio(root).alignLeft().text(CoreMessages.GradleDistribution_Label_SpecificGradleVersion).control();
        this.gradleVersionCombo = uiBuilderFactory.newCombo(root).alignLeft().disabled().control();
        this.gradleVersionCombo.setSize(150, this.gradleVersionCombo.getSize().y);
        this.gradleVersionCombo.setItems(getGradleVersions());
        if (this.gradleVersionCombo.getItemCount() > 0) {
            this.gradleVersionCombo.select(0);
        }
        uiBuilderFactory.span(root);

        List<Button> allRadios = ImmutableList
                .of(this.useGradleWrapperOption, this.useLocalInstallationDirOption, this.useRemoteDistributionUriOption, this.useGradleVersionOption);

        // setup initial state for the radio buttons and the configuration fields
        GradleDistributionWrapper wrapper = getConfiguration().getGradleDistribution().getValue();
        DistributionType type = wrapper.getType();
        String configuration = Strings.nullToEmpty(wrapper.getConfiguration());
        switch (type) {
            case WRAPPER:
                updateSelectedState(this.useGradleWrapperOption, allRadios);
                break;
            case LOCAL_INSTALLATION:
                updateSelectedState(this.useLocalInstallationDirOption, allRadios);
                this.localInstallationDirText.setText(configuration);
                break;
            case REMOTE_DISTRIBUTION:
                updateSelectedState(this.useRemoteDistributionUriOption, allRadios);
                this.remoteDistributionUriText.setText(configuration);
                break;
            case VERSION:
                updateSelectedState(this.useGradleVersionOption, allRadios);
                updateVersionSelection(configuration);
                break;
            default:
                throw new GradlePluginsRuntimeException("Unrecognized Gradle distribution type: " + type); //$NON-NLS-1$
        }

        // update the enabled/disabled state when the currently selected radio is changing
        new Enabler(this.useGradleWrapperOption).enables();
        new Enabler(this.useLocalInstallationDirOption).enables(this.localInstallationDirText, localInstallationDirBrowseButton);
        new Enabler(this.useRemoteDistributionUriOption).enables(this.remoteDistributionUriText);
        new Enabler(this.useGradleVersionOption).enables(this.gradleVersionCombo);
    }

    private String[] getGradleVersions() {
        return FluentIterable.from(this.publishedGradleVersions.getVersions()).transform(new Function<GradleVersion, String>() {

            @Override
            public String apply(GradleVersion gradleVersion) {
                return gradleVersion.getVersion();
            }
        }).toArray(String.class);
    }

    private void updateSelectedState(Button selectedRadioButton, List<Button> allRadioButtons) {
        for (Button radioButton : allRadioButtons) {
            if (radioButton.equals(selectedRadioButton)) {
                radioButton.setSelection(true);
                radioButton.setFocus();
            } else {
                radioButton.setSelection(false);
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

    private void bindGradleDistributionToConfiguration() {
        // add modify listeners to the texts and to the combo box
        this.localInstallationDirText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION,
                        Strings.emptyToNull(GradleOptionsWizardPage.this.localInstallationDirText.getText()));
                getConfiguration().setGradleDistribution(gradleDistribution);
            }
        });
        this.remoteDistributionUriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION,
                        Strings.emptyToNull(GradleOptionsWizardPage.this.remoteDistributionUriText.getText()));
                getConfiguration().setGradleDistribution(gradleDistribution);
            }
        });
        this.gradleVersionCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                GradleDistributionWrapper gradleDistribution = getSpecificVersion();
                getConfiguration().setGradleDistribution(gradleDistribution);
            }
        });
        this.gradleVersionCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                GradleDistributionWrapper gradleDistribution = getSpecificVersion();
                getConfiguration().setGradleDistribution(gradleDistribution);
            }
        });

        // add listeners to the radio buttons
        this.useGradleWrapperOption.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (GradleOptionsWizardPage.this.useGradleWrapperOption.getSelection()) {
                    GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(DistributionType.WRAPPER, null);
                    getConfiguration().setGradleDistribution(gradleDistribution);
                }
            }
        });
        this.useLocalInstallationDirOption.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (GradleOptionsWizardPage.this.useLocalInstallationDirOption.getSelection()) {
                    GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION,
                            Strings.emptyToNull(GradleOptionsWizardPage.this.localInstallationDirText.getText()));
                    getConfiguration().setGradleDistribution(gradleDistribution);
                }
            }
        });
        this.useRemoteDistributionUriOption.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (GradleOptionsWizardPage.this.useRemoteDistributionUriOption.getSelection()) {
                    GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION,
                            Strings.emptyToNull(GradleOptionsWizardPage.this.remoteDistributionUriText.getText()));
                    getConfiguration().setGradleDistribution(gradleDistribution);
                }
            }
        });
        this.useGradleVersionOption.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (GradleOptionsWizardPage.this.useGradleVersionOption.getSelection()) {
                    GradleDistributionWrapper gradleDistribution = getSpecificVersion();
                    getConfiguration().setGradleDistribution(gradleDistribution);
                }
            }
        });
    }

    private GradleDistributionWrapper getSpecificVersion() {
        int selectionIndex = this.gradleVersionCombo.getSelectionIndex();
        return GradleDistributionWrapper.from(DistributionType.VERSION,
                Strings.emptyToNull(selectionIndex == -1 || !Strings.isNullOrEmpty(this.gradleVersionCombo.getText()) ?
                        this.gradleVersionCombo.getText() :
                        this.gradleVersionCombo.getItem(selectionIndex)));
    }

    @Override
    protected String getPageContextInformation() {
        return this.pageContextInformation;
    }

}
