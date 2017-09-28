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
import org.eclipse.buildship.core.configuration.GradleProjectNatureConfiguredEvent;
import org.eclipse.buildship.core.configuration.GradleProjectNatureDeconfiguredEvent;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;

/**
 * Launch listener updating the classpath provider attribute.
 *
 * @author Donat Csikos
 */
public final class LaunchConfigurationListener implements ILaunchConfigurationListener, EventListener {

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

    @Override
    public void onEvent(Event event) {
        if (event instanceof GradleProjectNatureConfiguredEvent) {
            CorePlugin.externalLaunchConfigurationManager().updateClasspathProviders(((GradleProjectNatureConfiguredEvent)event).getProject());
        } else if (event instanceof GradleProjectNatureDeconfiguredEvent) {
            CorePlugin.externalLaunchConfigurationManager().updateClasspathProviders(((GradleProjectNatureDeconfiguredEvent)event).getProject());
        }
    }

    public static LaunchConfigurationListener createAndRegister() {
        LaunchConfigurationListener listener = new LaunchConfigurationListener();
        DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(listener);
        CorePlugin.listenerRegistry().addEventListener(listener);
        return listener;
    }

    public void unregister() {
        CorePlugin.listenerRegistry().removeEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
    }
}
