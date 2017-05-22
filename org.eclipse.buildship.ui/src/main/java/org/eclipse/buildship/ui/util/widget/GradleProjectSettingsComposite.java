/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.util.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Widget containing the following Gradle configuration elements:
 * <ul>
 * <li>Gradle distribution</li>
 * <li>Gradle user home</li>
 * <li>Build scans enablement</li>
 * <li>Offline mode enablement</li>
 * </ul>
 *
 * @author donat
 *
 */
public class GradleProjectSettingsComposite extends Composite {

    private final String overrideLabel;
    private final String preferencesLabel;

    private Button overrideBuildSettingsCheckbox;
    private Link parentPreferenceLink;
    private GradleDistributionGroup gradleDistributionGroup;
    private GradleUserHomeGroup gradleUserHomeGroup;
    private Button offlineModeCheckbox;
    private Button buildScansCheckbox;

    private GradleProjectSettingsComposite(Composite parent, boolean hasOverrideCheckbox, String overrideLabel, String preferencesLabel) {
        super(parent, SWT.NONE);

        this.overrideLabel = overrideLabel;
        this.preferencesLabel = preferencesLabel;

        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(this);

        if (hasOverrideCheckbox) {
            createOverrideWorkspaceCheckbox(this);
            configureParentPreferenceLink(this);
            createHorizontalLine(this);
        }
        createGradleDistributionGroup(this);
        createGradleUserHomeGroup(this);
        createOfflineModeCheckbox(this);
        createBuildScansEnabledCheckbox(this);

        Dialog.applyDialogFont(this);
    }

    private void createOverrideWorkspaceCheckbox(Composite parent) {
        this.overrideBuildSettingsCheckbox = new Button(parent, SWT.CHECK);
        this.overrideBuildSettingsCheckbox.setText(this.overrideLabel);
        GridDataFactory.swtDefaults().applyTo(parent);
    }

    private void configureParentPreferenceLink(Composite parent) {
        this.parentPreferenceLink = new Link(parent, SWT.NONE);
        this.parentPreferenceLink.setFont(parent.getFont());
        this.parentPreferenceLink.setText("<A>" + this.preferencesLabel + "...</A>");
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(this.parentPreferenceLink);
    }

    private void createHorizontalLine(Composite parent) {
        Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(line);
    }

    private void createGradleDistributionGroup(Composite parent) {
        this.gradleDistributionGroup = new GradleDistributionGroup(CorePlugin.publishedGradleVersions(), parent);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.gradleDistributionGroup);
    }

    private void createGradleUserHomeGroup(Composite parent) {
        this.gradleUserHomeGroup = new GradleUserHomeGroup(parent, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.gradleUserHomeGroup);
    }

    private void createOfflineModeCheckbox(Composite parent) {
        this.offlineModeCheckbox = new Button(parent, SWT.CHECK);
        this.offlineModeCheckbox.setText("Offline Mode");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.offlineModeCheckbox);
    }

    private void createBuildScansEnabledCheckbox(Composite parent) {
        this.buildScansCheckbox = new Button(parent, SWT.CHECK);
        this.buildScansCheckbox.setText("Build Scans");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.buildScansCheckbox);
        HoverText.createAndAttach(this.buildScansCheckbox, CoreMessages.Preference_Label_BuildScansHover);
    }


    private void setupEnablement() {
        // TODO (donat) implement
        // this.overrideProjectSettingsEnabler = new Enabler(this.overrideBuildSettingsCheckbox).enables(this.offlineModeCheckbox, this.buildScansEnabledCheckbox, this.gradleDistributionGroup);
    }

    public void updateEnablement() {
        // TODO (donat) implement
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public Button getOverrideBuildSettingsCheckbox() {
        return this.overrideBuildSettingsCheckbox;
    }

    public Link getParentPreferenceLink() {
        return this.parentPreferenceLink;
    }

    public GradleDistributionGroup getGradleDistributionGroup() {
        return this.gradleDistributionGroup;
    }

    public GradleUserHomeGroup getGradleUserHomeGroup() {
        return this.gradleUserHomeGroup;
    }

    public Button getOfflineModeCheckbox() {
        return this.offlineModeCheckbox;
    }

    public Button getBuildScansCheckbox() {
        return this.buildScansCheckbox;
    }

    public static GradleProjectSettingsComposite withOverrideCheckbox(Composite parent, String overrideLabel, String preferencesLabel) {
        return new GradleProjectSettingsComposite(parent, true, overrideLabel, preferencesLabel);
    }

    public static GradleProjectSettingsComposite withoutOverrideCheckbox(Composite parent) {
        return new GradleProjectSettingsComposite(parent, false, null, null);
    }
}
