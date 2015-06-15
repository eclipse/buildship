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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Removes all finished {@link ILaunch} instances associated with a {@link GradleConsole} instance.
 * The action is only enabled if at least one console can be removed.
 */
public final class RemoveAllTerminatedGradleConsolesAction extends Action implements ILaunchesListener2 {

    private final GradleConsole gradleConsole;

    public RemoveAllTerminatedGradleConsolesAction(GradleConsole gradleConsole) {
        this.gradleConsole = Preconditions.checkNotNull(gradleConsole);

        setToolTipText(ConsoleMessages.Action_RemoveAllTerminatedConsoles_Tooltip);
        setImageDescriptor(PluginImages.REMOVE_ALL_CONSOLES.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_ALL_CONSOLES.withState(ImageState.DISABLED).getImageDescriptor());

        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);

        update();
    }

    private void update() {
        setEnabled(this.gradleConsole.isCloseable() && this.gradleConsole.isTerminated());
    }

    private ImmutableList<GradleConsole> getTerminatedConsoles() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        return FluentIterable.of(consoleManager.getConsoles()).filter(GradleConsole.class).filter(new Predicate<GradleConsole>() {

            @Override
            public boolean apply(GradleConsole console) {
                return console.isCloseable() && console.isTerminated();
            }
        }).toList();
    }

    @Override
    public void run() {
        ImmutableList<GradleConsole> terminatedConsoles = getTerminatedConsoles();
        ConsolePlugin.getDefault().getConsoleManager().removeConsoles(terminatedConsoles.toArray(new GradleConsole[terminatedConsoles.size()]));
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
