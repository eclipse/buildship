/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild.testing;

import org.eclipse.jdt.internal.junit.model.ITestRunListener2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class EclipseTestListener implements ITestRunListener2 {

    interface EclipseTestEvent {
    } // TODO move to separate file and add implementations as inner clases

    static class TestRunStartedEvent implements EclipseTestEvent {
    }

    static class TestRunEndedEvent implements EclipseTestEvent {
    }

    static class TestStartedEvent implements EclipseTestEvent {

        private final String testId;
        private final String testName;

        public TestStartedEvent(String testId, String testName) {
            this.testId = testId;
            this.testName = testName;
        }

        public String getTestId() {
            return testId;
        }

        public String getTestName() {
            return testName;
        }
    }

    static class TestEndedEvent implements EclipseTestEvent {

        private final String testId;
        private final String testName;

        public TestEndedEvent(String testId, String testName) {
            this.testId = testId;
            this.testName = testName;
        }

        public String getTestId() {
            return testId;
        }

        public String getTestName() {
            return testName;
        }
    }

    static class TestFailedEvent implements EclipseTestEvent {
        private final int status;
        private final String testId;
        private final String testName;
        private final String trace;
        private final String expected;
        private final String actual;

        public TestFailedEvent(int status, String testId, String testName, String trace, String expected, String actual) {
            this.status = status;
            this.testId = testId;
            this.testName = testName;
            this.trace = trace;
            this.expected = expected;
            this.actual = actual;
        }

        public int getStatus() {
            return status;
        }

        public String getTestId() {
            return testId;
        }

        public String getTestName() {
            return testName;
        }

        public String getTrace() {
            return trace;
        }

        public String getExpected() {
            return expected;
        }

        public String getActual() {
            return actual;
        }
    }

    private final BlockingQueue<EclipseTestEvent> queue;

    public EclipseTestListener() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<EclipseTestEvent> getQueue() {
        return queue;
    }

    @Override
    public synchronized void testRunStarted(int testCount) {
        offer(new TestRunStartedEvent());
    }

    @Override
    public synchronized void testRunEnded(long elapsedTime) {
        offer(new TestRunEndedEvent());
    }

    @Override
    public synchronized void testRunStopped(long elapsedTime) {
        // TODO report failure when stopped?
        testRunEnded(elapsedTime);
    }

    @Override
    public synchronized void testRunTerminated() {
        // TODO report failure when terminated?
        testRunEnded(0);
    }

    @Override
    public synchronized void testStarted(String testId, String testName) {
        offer(new TestStartedEvent(testId, testName));
    }

    @Override
    public synchronized void testEnded(String testId, String testName) {
        offer(new TestEndedEvent(testId, testName));
    }

    @Override
    public synchronized void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
        offer(new TestFailedEvent(status, testId, testName, trace, expected, actual));
    }

    @Override
    public synchronized void testReran(String testId, String testClass, String testName, int status, String trace, String expected, String actual) {
        throw new UnsupportedOperationException("Unexpected call to testReran when running tests in Eclipse.");
    }

    @Override
    public synchronized void testTreeEntry(String description) {
    }

    private void offer(EclipseTestEvent event) {
        try {
            queue.offer(event, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // no interrupts expected
        }
    }
}
