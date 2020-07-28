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

import java.io.IOException;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.Destination;
import org.gradle.tooling.events.test.TestOutputEvent;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.console.ProcessStreams;
import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.launch.ExecuteLaunchRequestEvent;

/**
 * Forwards test output events to the Gradle console.
 */
public final class TestOutputForwardingEventListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof ExecuteLaunchRequestEvent) {
            handleLaunchRequest((ExecuteLaunchRequestEvent) event);
        }
    }

    private void handleLaunchRequest(final ExecuteLaunchRequestEvent event) {
        event.getOperation().addProgressListener(new ForwardingListener(event.getProcessDescription()));
    }

    private static class ForwardingListener implements org.gradle.tooling.events.ProgressListener {

        private final ProcessDescription processDescription;
        private ProcessStreams processStreams;

        public ForwardingListener(ProcessDescription processDescription) {
            this.processDescription = processDescription;
        }

        @Override
        public void statusChanged(ProgressEvent event) {
            if (event instanceof TestOutputEvent) {
                try {
                    printToConsole((TestOutputEvent) event);
                } catch (IOException e) {
                    CorePlugin.logger().warn("Cannot print test output to console", e);
                }
            }
        }

        private void printToConsole(TestOutputEvent testOutputEvent) throws IOException {
            if (this.processStreams == null) {
                this.processStreams = CorePlugin.processStreamsProvider().getOrCreateProcessStreams(this.processDescription);
            }
            String message = testOutputEvent.getDescriptor().getMessage();
            Destination destination = testOutputEvent.getDescriptor().getDestination();
            switch (destination) {
                case StdOut:
                    this.processStreams.getOutput().write(message.getBytes());
                    break;
                case StdErr:
                    this.processStreams.getError().write(message.getBytes());
                    break;
                default:
                    throw new IllegalStateException("Invalid destination: " + destination);
            }
        }
    }
}
