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

import com.google.common.base.Preconditions;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider;

/**
 * Provider of {@link ProcessStreams} instances that are backed by console pages of the Eclipse
 * Console view.
 */
public final class ConsoleProcessStreamsProvider implements ProcessStreamsProvider {

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
        return createAndRegisterNewConsole(processDescription);
    }

    private static GradleConsole createAndRegisterNewConsole(ProcessDescription processDescription) {
        // creates a new console and adds it to the Eclipse Console view
        GradleConsole gradleConsole = new GradleConsole(processDescription);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { gradleConsole });
        return gradleConsole;
    }

    private static GradleConsole createAndRegisterNewConsole(String name) {
        // creates a new console without a process description and adds it to the Eclipse Console view
        GradleConsole gradleConsole = new GradleConsole(name);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { gradleConsole });
        return gradleConsole;
    }

    /**
     * Holds a {@code GradleConsole} instance. The instance held by this inner class is not created until first accessed.
     */
    private static final class BackgroundJobProcessStream {

        private static final GradleConsole INSTANCE = createAndRegisterNewConsole(ConsoleMessages.Background_Console_Title);

    }

}
