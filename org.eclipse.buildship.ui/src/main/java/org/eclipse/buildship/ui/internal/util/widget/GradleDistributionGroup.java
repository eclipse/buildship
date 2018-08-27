/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.util.widget;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistributionInfo;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistributionType;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.font.FontUtils;
import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder.UiBuilderFactory;

/**
 * Composite to select a Gradle distribution.
 *
 * @author Donat Csikos
 */
public final class GradleDistributionGroup extends Group {

    /**
     * Event raised when the distribution changes in the widget.
     */
    public interface DistributionChangedListener {
        void distributionUpdated(GradleDistributionInfo distributionInfo);
    }

    private final List<DistributionChangedListener> listeners;
    private final PublishedGradleVersionsWrapper publishedGradleVersions;

    private Font font;

    private Text localInstallationDirText;
    private Button localInstallationDirBrowseButton;
    private Label localInstallationWarningLabel;
    private Text remoteDistributionUriText;
    private Combo gradleVersionCombo;
    private Button useGradleWrapperOption;
    private Button useLocalInstallationDirOption;
    private Button useRemoteDistributionUriOption;
    private Button useGradleVersionOption;


    public GradleDistributionGroup(PublishedGradleVersionsWrapper publishedGradleVersions, Composite parent) {
        super(parent, SWT.NONE);
        setText(CoreMessages.GradleDistribution_Label_Group);

        this.listeners = new LinkedList<>();
        this.publishedGradleVersions = publishedGradleVersions;

        createWidgets();
        updateEnablement();
        addListeners();
    }

    private void createWidgets() {
        setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        setLayout(LayoutUtils.newGridLayout(4));

        this.font = FontUtils.getDefaultDialogFont();
        UiBuilderFactory uiBuilder = new UiBuilder.UiBuilderFactory(this.font);
        this.useGradleWrapperOption = uiBuilder.newRadio(this).alignLeft(4).text(CoreMessages.GradleDistribution_Label_GradleWrapper).control();

        this.useLocalInstallationDirOption = uiBuilder.newRadio(this).alignLeft().text(CoreMessages.GradleDistribution_Label_LocalInstallationDirectory).control();
        this.localInstallationDirText = uiBuilder.newText(this).alignFillHorizontal().disabled().control();
        this.localInstallationDirBrowseButton = uiBuilder.newButton(this).alignLeft().disabled().text(UiMessages.Button_Label_Browse).control();
        this.localInstallationDirBrowseButton.addSelectionListener(new DirectoryDialogSelectionListener(this.getShell(), this.localInstallationDirText, CoreMessages.GradleDistribution_Label_LocalInstallationDirectory));
        this.localInstallationWarningLabel = uiBuilder.newLabel(this).alignRight().control();
        this.localInstallationWarningLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
        HoverText.createAndAttach(this.localInstallationWarningLabel, NLS.bind(CoreMessages.WarningMessage_Using_0_NonPortable, "local Gradle distribution"));

        this.useRemoteDistributionUriOption = uiBuilder.newRadio(this).alignLeft().text(CoreMessages.GradleDistribution_Label_RemoteDistributionUri).control();
        this.remoteDistributionUriText = uiBuilder.newText(this).alignFillHorizontal().disabled().control();

        uiBuilder.span(this);
        uiBuilder.span(this);

        this.useGradleVersionOption = uiBuilder.newRadio(this).alignLeft().text(CoreMessages.GradleDistribution_Label_SpecificGradleVersion).control();
        this.gradleVersionCombo = uiBuilder.newCombo(this).alignFillHorizontal().disabled().control();
        this.gradleVersionCombo.setSize(150, this.gradleVersionCombo.getSize().y);
        this.gradleVersionCombo.setItems(getGradleVersions());
        if (this.gradleVersionCombo.getItemCount() > 0) {
            this.gradleVersionCombo.select(0);
        }
    }

    private void updateEnablement() {
        boolean groupEnabled = getEnabled();
        this.useGradleWrapperOption.setEnabled(groupEnabled);
        this.useLocalInstallationDirOption.setEnabled(groupEnabled);
        this.useRemoteDistributionUriOption.setEnabled(groupEnabled);
        this.useGradleVersionOption.setEnabled(groupEnabled);

        this.localInstallationDirText.setEnabled(groupEnabled && this.useLocalInstallationDirOption.getSelection());
        this.localInstallationDirBrowseButton.setEnabled(groupEnabled && this.useLocalInstallationDirOption.getSelection());
        this.remoteDistributionUriText.setEnabled(groupEnabled && this.useRemoteDistributionUriOption.getSelection());
        this.gradleVersionCombo.setEnabled(groupEnabled && this.useGradleVersionOption.getSelection());
    }

    public GradleDistributionInfo getDistributionInfo() {
        if (this.useGradleWrapperOption.getSelection()) {
            return new GradleDistributionInfo(GradleDistributionType.WRAPPER, "");
        } else if (this.useLocalInstallationDirOption.getSelection()) {
            return new GradleDistributionInfo(GradleDistributionType.LOCAL_INSTALLATION, this.localInstallationDirText.getText());
        } else if (this.useRemoteDistributionUriOption.getSelection()) {
            return new GradleDistributionInfo(GradleDistributionType.REMOTE_DISTRIBUTION, this.remoteDistributionUriText.getText());
        } else if (this.useGradleVersionOption.getSelection()) {
            return new GradleDistributionInfo(GradleDistributionType.VERSION, getSpecificVersion());
        } else {
            throw new GradlePluginsRuntimeException("No Gradle distribution type selected");
        }
    }

    private String getSpecificVersion() {
        int selectionIndex = this.gradleVersionCombo.getSelectionIndex();
        return Strings.emptyToNull(selectionIndex == -1 || !Strings.isNullOrEmpty(this.gradleVersionCombo.getText()) ?
                this.gradleVersionCombo.getText() :
                this.gradleVersionCombo.getItem(selectionIndex));
    }

    public void setDistributionInfo(GradleDistributionInfo distributionInfo) {
        GradleDistributionType type = distributionInfo.getType();
        String configuration = Strings.nullToEmpty(distributionInfo.getConfiguration());
        ImmutableList<Button> allRadios = ImmutableList.of(this.useGradleWrapperOption, this.useLocalInstallationDirOption, this.useRemoteDistributionUriOption, this.useGradleVersionOption);
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

        updateEnablement();
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

    private void addListeners() {
        NotifyingListener listener = new NotifyingListener();
        this.localInstallationDirText.addModifyListener(listener);
        this.remoteDistributionUriText.addModifyListener(listener);
        this.gradleVersionCombo.addModifyListener(listener);
        this.gradleVersionCombo.addSelectionListener(listener);
        this.useGradleWrapperOption.addSelectionListener(listener);
        this.useLocalInstallationDirOption.addSelectionListener(listener);
        this.useRemoteDistributionUriOption.addSelectionListener(listener);
        this.useGradleVersionOption.addSelectionListener(listener);
    }

    public void addDistributionChangedListener(DistributionChangedListener listener) {
        this.listeners.add(listener);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateEnablement();
    }

    @Override
    public void dispose() {
        if (this.font != null && !this.font.isDisposed()) {
            this.font.dispose();
        }
        super.dispose();
    }

    /**
     * Notifies listeners when the selected Gradle distribution changes.
     */
    private final class NotifyingListener extends SelectionAdapter implements ModifyListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateAndNotify();
        }

        @Override
        public void modifyText(ModifyEvent e) {
           updateAndNotify();
        }

        private void updateAndNotify() {
            updateEnablement();
            for (DistributionChangedListener listener : GradleDistributionGroup.this.listeners) {
                listener.distributionUpdated(getDistributionInfo());
            }
        }
    }
}
