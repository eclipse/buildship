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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.console.ProcessDescription;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;

/**
 * Provides a console to display the output of interacting with Gradle.
 *
 * Note that once a console is removed, all open streams managed by the console will be closed
 * automatically, thus there is no need for us to close these streams explicitly here.
 */
public final class GradleConsole extends IOConsole implements ProcessStreams {

    private final ProcessDescription processDescription;
    private final IOConsoleOutputStream outputStream;
    private final IOConsoleOutputStream errorStream;
    private final IOConsoleInputStream inputStream;

    public GradleConsole(ProcessDescription processDescription) {
        super(processDescription.getName(), PluginImages.TASK.withState(PluginImages.ImageState.ENABLED).getImageDescriptor());

        this.processDescription = processDescription;
        this.outputStream = newOutputStream();
        this.errorStream = newOutputStream();
        this.inputStream = super.getInputStream();

        // set proper colors on output/error streams (needs to happen in the UI thread)
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Color outputColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
                GradleConsole.this.outputStream.setColor(outputColor);

                Color errorColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
                GradleConsole.this.errorStream.setColor(errorColor);
            }
        });
    }

    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }

    public boolean isTerminated() {
        Optional<ILaunch> launch = this.processDescription.getLaunch();
        return launch.isPresent() && launchFinished(launch.get());
    }

    private boolean launchFinished(ILaunch launch) {
        // a launch is considered finished, if it is not registered anymore
        // (all other ways to determine the state of the launch did not work for us)
        return !ImmutableList.copyOf(DebugPlugin.getDefault().getLaunchManager().getLaunches()).contains(launch);
    }

    public boolean isCloseable() {
        return this.processDescription.getLaunch().isPresent();
    }

    @Override
    public OutputStream getOutput() {
        return this.outputStream;
    }

    @Override
    public OutputStream getError() {
        return this.errorStream;
    }

    @Override
    public InputStream getInput() {
        return this.inputStream;
    }

    @Override
    public void close() {
        Exception e = null;

        try {
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException ioe) {
            e = ioe;
        }
        try {
            this.errorStream.flush();
            this.errorStream.close();
        } catch (IOException ioe) {
            e = ioe;
        }
        try {
            this.inputStream.close();
        } catch (IOException ioe) {
            e = ioe;
        }

        if (e != null) {
            String message = String.format("Cannot close streams of console %s.", getName()); //$NON-NLS-1$
            UiPlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

}
