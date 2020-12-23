/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.ui.internal.util.widget.HoverText;

/**
 * The main workspace preference page for Buildship. Currently only used to configure the Gradle
 * User Home.
 */
public final class GradleExperimentalFeaturesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String PAGE_ID = "org.eclipse.buildship.ui.preferences.experimental";
    private Button enableModuleSuppotCheckbox;

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 0).applyTo(composite);

        this.enableModuleSuppotCheckbox = new Button(parent, SWT.CHECK);
        this.enableModuleSuppotCheckbox.setText("Enable module support");
        this.enableModuleSuppotCheckbox.setSelection(CorePlugin.configurationManager().loadWorkspaceConfiguration().isExperimentalModuleSupportEnabled());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(this.enableModuleSuppotCheckbox);
        HoverText.createAndAttach(this.enableModuleSuppotCheckbox, "Add module dependencies to the modulepath");

        return composite;
    }

    @Override
    public boolean performOk() {
        ConfigurationManager manager = CorePlugin.configurationManager();
        WorkspaceConfiguration c = manager.loadWorkspaceConfiguration();
        manager.saveWorkspaceConfiguration(new WorkspaceConfiguration(c.getGradleDistribution(), c.getGradleUserHome(), c.getJavaHome(), c.isOffline(), c.isBuildScansEnabled(),
                c.isAutoSync(), c.getArguments(), c.getJvmArguments(), c.isShowConsoleView(), c.isShowExecutionsView(), this.enableModuleSuppotCheckbox.getSelection()));
        return true;
    }
}
