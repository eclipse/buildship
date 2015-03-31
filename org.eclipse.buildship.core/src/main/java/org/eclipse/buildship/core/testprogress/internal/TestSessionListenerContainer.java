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

package org.eclipse.buildship.core.testprogress.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.internal.junit.model.ITestSessionListener;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestElement;

/**
 * Notifies all registered {@code ITestSessionListener} instances about the test execution progress. This implementation hides the intricacies of how certain test (suite) states
 * can be triggered in the Eclipse Test Runner.
 */
@SuppressWarnings("restriction")
final class TestSessionListenerContainer {

    private final List<ITestSessionListener> listeners = new CopyOnWriteArrayList<ITestSessionListener>();

    void notifySessionStarted() {
        for (ITestSessionListener listener : this.listeners) {
            listener.sessionStarted();
        }
    }

    void notifySessionEnded(long elapsedTimeInSeconds) {
        for (ITestSessionListener listener : this.listeners) {
            listener.sessionEnded(elapsedTimeInSeconds);
        }
    }

    void notifyTestingBegins() {
        for (ITestSessionListener listener : this.listeners) {
            listener.runningBegins();
        }
    }

    void notifySuiteStarted(TestElement element) {
        waitForUiThreadToFinishUpdatingTestView();
        for (ITestSessionListener listener : this.listeners) {
            // there is no API to explicitly start a test suite, but adding the test suite suffices
            listener.testAdded(element);
        }
    }

    void notifySuiteFinished(TestElement element, TestElement.Status status, String trace, String expected, String actual) {
        for (ITestSessionListener listener : this.listeners) {
            // if an OK status is passed and the [trace, expected, actual] parameters are null the
            // suite will be actually marked as success
            listener.testFailed(element, status, trace, expected, actual);
        }
    }

    void notifyTestStarted(TestCaseElement element) {
        waitForUiThreadToFinishUpdatingTestView();
        for (ITestSessionListener listener : this.listeners) {
            // starting the test will implicitly add the the test
            listener.testStarted(element);
        }
    }

    void notifyTestEnded(TestCaseElement element) {
        for (ITestSessionListener listener : this.listeners) {
            listener.testEnded(element);
        }
    }

    void add(ITestSessionListener listener) {
        this.listeners.add(listener);
    }

    void remove(ITestSessionListener listener) {
        this.listeners.remove(listener);
    }

    private static void waitForUiThreadToFinishUpdatingTestView() {
        // the test view gets updated in the (separate) UI thread, this can lead to
        // race conditions with some suites/tests not being added to the view
        // briefly sleeping acts as a current work-around
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
