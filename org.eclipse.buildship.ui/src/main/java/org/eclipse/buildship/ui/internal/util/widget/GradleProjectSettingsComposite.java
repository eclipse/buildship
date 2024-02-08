/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.gradle.Pair;

/**
 * Widget containing the following Gradle configuration elements.
 * <ul>
 * <li>Gradle distribution</li>
 * <li>Gradle user home</li>
 * <li>Build scans enablement</li>
 * <li>Offline mode enablement</li>
 * </ul>
 *
 * @author Donat Csikos
 */
public final class GradleProjectSettingsComposite extends Composite {

    private final String overrideCheckboxLabel;
    private final String configureParentPrefsLinkLabel;

    private Button overrideSettingsCheckbox;
    private Link parentPreferenceLink;
    private GradleDistributionGroup gradleDistributionGroup;
    private AdvancedOptionsGroup advancedOptionsGroup;
    private Button offlineModeCheckbox;
    private Button buildScansCheckbox;
    private Button autoSyncCheckbox;
    private Button showConsoleViewCheckbox;
    private Button showExecutionsViewCheckbox;

    private GradleProjectSettingsComposite(Composite parent, boolean hasOverrideCheckbox, String overrideCheckboxLabel, String configureParentPrefsLinkLabel, boolean hasAutoSyncCheckbox, boolean variableSelector) {
        super(parent, SWT.NONE);

        this.overrideCheckboxLabel = overrideCheckboxLabel;
        this.configureParentPrefsLinkLabel = configureParentPrefsLinkLabel;

        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(this);

        if (hasOverrideCheckbox) {
            createOverrideWorkspaceCheckbox(this);
            configureParentPreferenceLink(this);
            createHorizontalLine(this);
        }
        createGradleDistributionGroup(this);
        createAdvancedOptionsGroup(this, variableSelector);
        createOfflineModeCheckbox(this);
        createBuildScansCheckbox(this);
        if (hasAutoSyncCheckbox) {
            createAutoSyncCheckbox(this);
        }
        createShowConsoleViewCheckbox(this);
        createShowExecutionsViewCheckbox(this);

        addListeners();

        Dialog.applyDialogFont(this);
    }

    private void createOverrideWorkspaceCheckbox(Composite parent) {
        this.overrideSettingsCheckbox = new Button(parent, SWT.CHECK);
        this.overrideSettingsCheckbox.setText(this.overrideCheckboxLabel);
        GridDataFactory.swtDefaults().applyTo(parent);
    }

    private void configureParentPreferenceLink(Composite parent) {
        this.parentPreferenceLink = new Link(parent, SWT.NONE);
        this.parentPreferenceLink.setFont(parent.getFont());
        this.parentPreferenceLink.setText("<A>" + this.configureParentPrefsLinkLabel + "...</A>");
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

    private void createAdvancedOptionsGroup(Composite parent, boolean variableSelector) {
        this.advancedOptionsGroup = new AdvancedOptionsGroup(parent, variableSelector);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.advancedOptionsGroup);
    }

    private void createOfflineModeCheckbox(Composite parent) {
        this.offlineModeCheckbox = new Button(parent, SWT.CHECK);
        this.offlineModeCheckbox.setText("Offline Mode");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.offlineModeCheckbox);
    }

    private void createBuildScansCheckbox(Composite parent) {
        this.buildScansCheckbox = new Button(parent, SWT.CHECK);
        this.buildScansCheckbox.setText("Build Scans");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.buildScansCheckbox);
        HoverText.createAndAttach(this.buildScansCheckbox, CoreMessages.Preference_Label_BuildScansHover);
    }

    private void createAutoSyncCheckbox(Composite parent) {
        this.autoSyncCheckbox = new Button(parent, SWT.CHECK);
        this.autoSyncCheckbox.setText(CoreMessages.Preference_Label_AutoSync);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(this.autoSyncCheckbox);
        HoverText.createAndAttach(this.autoSyncCheckbox, CoreMessages.Preference_Label_AutoSyncHover);
    }

    private void createShowConsoleViewCheckbox(Composite parent) {
        this.showConsoleViewCheckbox = new Button(parent, SWT.CHECK);
        this.showConsoleViewCheckbox.setText("Show Console View");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.showConsoleViewCheckbox);
        HoverText.createAndAttach(this.showConsoleViewCheckbox, CoreMessages.Preference_Label_ShowConsoleViewHover);
        new Label(parent, SWT.NONE).setVisible(false);
    }

    private void createShowExecutionsViewCheckbox(Composite parent) {
        this.showExecutionsViewCheckbox = new Button(parent, SWT.CHECK);
        this.showExecutionsViewCheckbox.setText("Show Executions View");
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.showExecutionsViewCheckbox);
        HoverText.createAndAttach(this.showExecutionsViewCheckbox, CoreMessages.Preference_Label_ShowExecutionsViewHover);
        new Label(parent, SWT.NONE).setVisible(false);
    }

    private void addListeners() {
        if (this.overrideSettingsCheckbox != null) {
            this.overrideSettingsCheckbox.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateEnablement();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    updateEnablement();
                }
            });
        }
    }

    public void updateEnablement() {
        if (this.overrideSettingsCheckbox != null) {
            boolean enabled = this.overrideSettingsCheckbox.getSelection();
            this.gradleDistributionGroup.setEnabled(enabled);
            this.advancedOptionsGroup.setEnabled(enabled);
            this.offlineModeCheckbox.setEnabled(enabled);
            this.buildScansCheckbox.setEnabled(enabled);
            if (this.autoSyncCheckbox != null) {
                this.autoSyncCheckbox.setEnabled(enabled);
            }
            this.showConsoleViewCheckbox.setEnabled(enabled);
            this.showExecutionsViewCheckbox.setEnabled(enabled);
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public Button getOverrideBuildSettingsCheckbox() {
        return this.overrideSettingsCheckbox;
    }

    public Link getParentPreferenceLink() {
        return this.parentPreferenceLink;
    }

    public GradleDistributionGroup getGradleDistributionGroup() {
        return this.gradleDistributionGroup;
    }

    public AdvancedOptionsGroup getAdvancedOptionsGroup() {
        return this.advancedOptionsGroup;
    }

    public Button getOfflineModeCheckbox() {
        return this.offlineModeCheckbox;
    }

    public Button getBuildScansCheckbox() {
        return this.buildScansCheckbox;
    }

    public Button getAutoSyncCheckbox() {
        return this.autoSyncCheckbox;
    }

    public Button getShowConsoleViewCheckbox() {
        return this.showConsoleViewCheckbox;
    }

    public Button getShowExecutionsViewCheckbox() {
        return this.showExecutionsViewCheckbox;
    }

    public static final GradleProjectSettingsCompositeBuilder builder(Composite parent) {
        return new GradleProjectSettingsCompositeBuilder(parent);
    }

    /**
     * Builder for building a {@link GradleProjectSettingsComposite}.
     *
     * @author Christopher Bryan Boyd (wodencafe)
     *
     */
    public static class GradleProjectSettingsCompositeBuilder {
        private Pair<String, String> overrideCheckbox = null;
        private boolean autoSyncCheckbox = false;
        private boolean variableSelector = false;
        private Composite parent;
        private GradleProjectSettingsCompositeBuilder(Composite parent) {
            this.parent = parent;
        }
        public GradleProjectSettingsCompositeBuilder withOverrideCheckbox(String overrideCheckboxLabel, String configureParentPrefsLinkLabel) {
            this.overrideCheckbox = new Pair<>(overrideCheckboxLabel, configureParentPrefsLinkLabel);
            return this;
        }
        public GradleProjectSettingsCompositeBuilder withAutoSyncCheckbox() {
            this.autoSyncCheckbox = true;
            return this;
        }
        public GradleProjectSettingsCompositeBuilder showVariableSelector() {
            this.variableSelector = true;
            return this;
        }

        public GradleProjectSettingsComposite build() {
            if (this.overrideCheckbox != null) {
                return new GradleProjectSettingsComposite(this.parent, true, this.overrideCheckbox.getFirst(), this.overrideCheckbox.getSecond(), this.autoSyncCheckbox, this.variableSelector);
            } else {
                return new GradleProjectSettingsComposite(this.parent, false, null, null, this.autoSyncCheckbox, this.variableSelector);
            }
        }
    }

}
