/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Launch listener updating the classpath provider attribute.
 *
 * @author Donat Csikos
 */
public class LaunchConfigurationListener implements ILaunchConfigurationListener {

    @Override
    public void launchConfigurationAdded(ILaunchConfiguration configuration) {
        CorePlugin.externalLaunchConfigurationManager().updateClasspathProvider(configuration);
    }

    @Override
    public void launchConfigurationChanged(ILaunchConfiguration configuration) {
        CorePlugin.externalLaunchConfigurationManager().updateClasspathProvider(configuration);
    }

    @Override
    public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
    }

    public static LaunchConfigurationListener createAndRegister() {
        LaunchConfigurationListener listener = new LaunchConfigurationListener();
        DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(listener);
        return listener;
    }

    public void unregister() {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
    }
}
