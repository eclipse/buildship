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

package org.eclipse.buildship.ui.internal.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Provides the tabs shown in the <i>Gradle Run Configuration</i> view.
 */
public final class GradleRunConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        // @formatter:off
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new ProjectTab(),
                new ProjectSettingsTab(),
                new JavaHomeTab(),
                new ArgumentsTab(),
                new CommonTab()
        };
        setTabs(tabs);
        // @formatter:on
    }

}
