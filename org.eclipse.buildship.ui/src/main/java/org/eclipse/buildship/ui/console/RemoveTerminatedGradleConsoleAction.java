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

package org.eclipse.buildship.ui.console;

import com.google.common.base.Preconditions;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Removes the finished {@link ILaunch} instance associated with a given {@link GradleConsole}
 * instance. The action is only enabled if the launch instance has terminated.
 *
 * Note: the implementation is somewhat along the lines of
 * {@code org.eclipse.debug.internal.ui.views.console.ConsoleRemoveLaunchAction}.
 */
public final class RemoveTerminatedGradleConsoleAction extends Action implements ILaunchesListener2 {

    private final GradleConsole gradleConsole;

    public RemoveTerminatedGradleConsoleAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(ConsoleMessages.Action_RemoveTerminatedConsole_Tooltip);
        setImageDescriptor(PluginImages.REMOVE_CONSOLE.withState(PluginImage.ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_CONSOLE.withState(PluginImage.ImageState.DISABLED).getImageDescriptor());

        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);

        update();
    }

    private void update() {
        setEnabled(this.gradleConsole.isCloseable() && this.gradleConsole.isTerminated());
    }

    @Override
    public void run() {
        ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { this.gradleConsole });
    }

    @Override
    public void launchesAdded(ILaunch[] launches) {
    }

    @Override
    public void launchesChanged(ILaunch[] launches) {
    }

    @Override
    public void launchesTerminated(ILaunch[] launches) {
    }

    @Override
    public void launchesRemoved(ILaunch[] launches) {
        update();
    }

    public void dispose() {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
    }

}
