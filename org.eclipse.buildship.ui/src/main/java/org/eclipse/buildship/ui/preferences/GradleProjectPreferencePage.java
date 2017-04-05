/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.ConfigurationManager;
import org.eclipse.buildship.ui.util.selection.Enabler;

/**
 * Preference page for Gradle projects.
 *
 * @author Donat Csikos
 */
public final class GradleProjectPreferencePage extends PropertyPage {

    private Button overrideWorkspaceSettingsCheckbox;
    private Button offlineModeCheckbox;
    private Button buildScansEnabledCheckbox;

    @Override
    protected Control createContents(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        page.setLayout(layout);

        Group overridePreferencesGroup = createGroup(page);
        createOverrideWorkspacePreferencesControl(overridePreferencesGroup);
        initialize();

        return page;
    }

    private Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private void createOverrideWorkspacePreferencesControl(Composite container) {
        GridLayout layout = new GridLayout(3, false);
        container.setLayout(layout);

        this.overrideWorkspaceSettingsCheckbox = new Button(container, SWT.CHECK);
        this.overrideWorkspaceSettingsCheckbox.setText("Override workspace settings");
        this.offlineModeCheckbox = new Button(container, SWT.CHECK);
        this.offlineModeCheckbox.setText("Offline Mode");
        this.buildScansEnabledCheckbox = new Button(container, SWT.CHECK);
        this.buildScansEnabledCheckbox.setText("Build Scans");

        new Enabler(this.overrideWorkspaceSettingsCheckbox).enables(this.offlineModeCheckbox, this.buildScansEnabledCheckbox);
    }


    private void initialize() {
        IProject project = getTargetProject();
        BuildConfiguration buildConfig = CorePlugin.configurationManager().loadProjectConfiguration(project).getBuildConfiguration();
        boolean overrideWorkspaceSettings = buildConfig.isOverrideWorkspaceSettings();
        this.overrideWorkspaceSettingsCheckbox.setSelection(overrideWorkspaceSettings);
        this.buildScansEnabledCheckbox.setEnabled(overrideWorkspaceSettings);
        this.buildScansEnabledCheckbox.setSelection(buildConfig.isBuildScansEnabled());
        this.offlineModeCheckbox.setEnabled(overrideWorkspaceSettings);
        this.offlineModeCheckbox.setSelection(buildConfig.isOfflineMode());
    }

    @Override
    public boolean performOk() {
       IProject project = getTargetProject();
       ConfigurationManager manager = CorePlugin.configurationManager();
       BuildConfiguration currentConfig = manager.loadProjectConfiguration(project).getBuildConfiguration();
       BuildConfiguration updatedConfig = manager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
                                                            currentConfig.getGradleDistribution(),
                                                            this.overrideWorkspaceSettingsCheckbox.getSelection(),
                                                            this.buildScansEnabledCheckbox.getSelection(),
                                                            this.offlineModeCheckbox.getSelection());
       manager.saveBuildConfiguration(updatedConfig);
       return true;
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private IProject getTargetProject() {
        return (IProject) Platform.getAdapterManager().getAdapter(getElement(), IProject.class);
    }
}
