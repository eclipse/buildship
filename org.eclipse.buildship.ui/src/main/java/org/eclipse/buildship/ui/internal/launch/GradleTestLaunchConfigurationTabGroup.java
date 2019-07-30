/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Provides the tabs for the Gradle Test launch configuration dialog.
 */
public final class GradleTestLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        // @formatter:off
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new TestsTab(),
                new ProjectSettingsTab(),
                new CommonTab()
        };
        setTabs(tabs);
        // @formatter:on
    }

}
