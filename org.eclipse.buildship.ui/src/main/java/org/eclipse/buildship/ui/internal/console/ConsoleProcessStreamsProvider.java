/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.console;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider;

/**
 * Provider of {@link ProcessStreams} instances that are backed by console pages of the Eclipse
 * Console view.
 */
public final class ConsoleProcessStreamsProvider implements ProcessStreamsProvider, IConsoleListener {

    private final Map<ProcessDescription, GradleConsole> consoles = new HashMap<>();

    private ConsoleProcessStreamsProvider() {
    }

    public static ConsoleProcessStreamsProvider create() {
        ConsoleProcessStreamsProvider provider = new ConsoleProcessStreamsProvider();
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(provider);
        return provider;
    }

    /**
     * Returns the same instance for each invocation.
     *
     * @return the instance suitable for background jobs
     */
    @Override
    public synchronized ProcessStreams getBackgroundJobProcessStreams() {
        // static inner class will be loaded lazily upon first access
        return BackgroundJobProcessStream.INSTANCE;
    }

    /**
     * Returns a new instance for each invocation.
     *
     * @param processDescription the backing process
     * @return the new instance
     */
    @Override
    public ProcessStreams createProcessStreams(ProcessDescription processDescription) {
        Preconditions.checkNotNull(processDescription);
        GradleConsole console = createNewConsole(processDescription);
        this.consoles.put(processDescription, console);
        return console;
    }

    @Override
    public ProcessStreams getOrCreateProcessStreams(ProcessDescription processDescription) {
        Preconditions.checkNotNull(processDescription);
        GradleConsole existingConsole = this.consoles.get(processDescription);
        if (existingConsole == null) {
            GradleConsole newConsole = createNewConsole(processDescription);
            this.consoles.put(processDescription, newConsole);
            return newConsole;
        } else {
            return existingConsole;
        }
    }

    private static GradleConsole createNewConsole(ProcessDescription processDescription) {
        // creates a new console and adds it to the Eclipse Console view
        GradleConsole gradleConsole = new GradleConsole(processDescription);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { gradleConsole });
        return gradleConsole;
    }

    private static GradleConsole createNewConsole(String name) {
        // creates a new console without a process description and adds it to the Eclipse Console view
        GradleConsole gradleConsole = new GradleConsole(name);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { gradleConsole });
        return gradleConsole;
    }

    /**
     * Holds a {@code GradleConsole} instance. The instance held by this inner class is not created until first accessed.
     */
    private static final class BackgroundJobProcessStream {

        private static final GradleConsole INSTANCE = createNewConsole(ConsoleMessages.Background_Console_Title);

    }

    @Override
    public void consolesAdded(IConsole[] consoles) {
        // only this class creates new consoles
    }

    @Override
    public void consolesRemoved(IConsole[] consoles) {
        for (IConsole console : consoles) {
            if (console instanceof GradleConsole) {
                Optional<ProcessDescription> description = ((GradleConsole)console).getProcessDescription();
                if (description.isPresent()) {
                    this.consoles.remove(description.get());
                }
            }
        }

    }

}
