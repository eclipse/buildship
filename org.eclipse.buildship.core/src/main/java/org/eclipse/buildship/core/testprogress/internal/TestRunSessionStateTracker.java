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

/**
 * Keeps track of the current state of a test run session.
 */
final class TestRunSessionStateTracker {

    /**
     * Enumerates the different states a test runner session can be in.
     */
    enum TestRunSessionState {
        UNKNOWN, RUNNING, STOPPED, FINISHED
    }

    private TestRunSessionState currentState;
    private long startTime;
    private long endTime;

    TestRunSessionStateTracker() {
        this.currentState = TestRunSessionState.UNKNOWN;
        this.startTime = -1;
        this.endTime = -1;
    }

    TestRunSessionState getCurrentState() {
        return this.currentState;
    }

    void sessionStarted() {
        this.startTime = System.currentTimeMillis();
        this.currentState = TestRunSessionState.RUNNING;
    }

    void sessionFinished() {
        this.endTime = System.currentTimeMillis();
        this.currentState = TestRunSessionState.FINISHED;
    }

    boolean isRunning() {
        return this.currentState == TestRunSessionState.RUNNING;
    }

    boolean isStopped() {
        return this.currentState == TestRunSessionState.STOPPED;
    }

    long getDuration() {
        return this.endTime - this.startTime;
    }

}
