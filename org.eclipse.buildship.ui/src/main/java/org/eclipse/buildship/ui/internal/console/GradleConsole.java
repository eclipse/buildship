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

package org.eclipse.buildship.ui.internal.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Optional;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.UiPlugin;

/**
 * Provides a console to display the output of interacting with Gradle.
 *
 * Note that once a console is removed, all open streams managed by the console will be closed
 * automatically, thus there is no need for us to close these streams explicitly here.
 */
public final class GradleConsole extends IOConsole implements ProcessStreams {

    private final Optional<ProcessDescription> processDescription;
    private final IOConsoleOutputStream configurationStream;
    private final IOConsoleOutputStream outputStream;
    private final IOConsoleOutputStream errorStream;
    private final IOConsoleInputStream inputStream;

    public GradleConsole(String name) {
        this(name, Optional.<ProcessDescription>absent());
    }

    public GradleConsole(ProcessDescription processDescription) {
        this(processDescription.getName(), Optional.of(processDescription));
    }

    private GradleConsole(String name, Optional<ProcessDescription> processDescription) {
        super(name, PluginImages.TASK.withState(PluginImages.ImageState.ENABLED).getImageDescriptor());

        this.processDescription = processDescription;
        this.configurationStream = newOutputStream();
        this.outputStream = newOutputStream();
        this.errorStream = newOutputStream();
        this.inputStream = super.getInputStream();

        // decorate console output such that URLs are presented as clickable links
        addPatternMatchListener(new UrlPatternMatchListener());

        // collect build scan URL
        addPatternMatchListener(new BuildScanPatternMatchListener());

        // set proper colors on output/error streams (needs to happen in the UI thread)
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @SuppressWarnings("restriction")
            @Override
            public void run() {
                Font consoleFont = JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT);
                GradleConsole.this.setFont(consoleFont);

                Color backgroundColor = DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR);
                GradleConsole.this.setBackground(backgroundColor);

                Color inputColor = DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR);
                GradleConsole.this.inputStream.setColor(inputColor);

                Color outputColor = DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR);
                GradleConsole.this.outputStream.setColor(outputColor);

                Color errorColor = DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR);
                GradleConsole.this.errorStream.setColor(errorColor);

                // assign a static color to the configuration output stream
                Color configurationColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
                GradleConsole.this.configurationStream.setColor(configurationColor);
            }
        });
    }

    public Optional<ProcessDescription> getProcessDescription() {
        return this.processDescription;
    }

    public boolean isTerminated() {
        return this.processDescription.isPresent() && this.processDescription.get().getJob().getState() == Job.NONE;
    }

    public boolean isCloseable() {
        return this.processDescription.isPresent();
    }

    @Override
    public OutputStream getConfiguration() {
        return this.configurationStream;
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
            this.configurationStream.flush();
            this.configurationStream.close();
        } catch (IOException ioe) {
            e = ioe;
        }
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
