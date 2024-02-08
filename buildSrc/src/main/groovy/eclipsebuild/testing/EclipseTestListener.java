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

    private final BlockingQueue<EclipseTestEvent> queue;

    public EclipseTestListener() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<EclipseTestEvent> getQueue() {
        return queue;
    }

    @Override
    public synchronized void testRunStarted(int testCount) {
        offer(new EclipseTestEvent.TestRunStarted(testCount));
    }

    @Override
    public synchronized void testRunEnded(long elapsedTime) {
        offer(new EclipseTestEvent.TestRunEnded());
    }

    @Override
    public void testTreeEntry(String description) {
        offer(new EclipseTestEvent.TestTreeEntry(description));
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
        offer(new EclipseTestEvent.TestStarted(testId, testName));
    }

    @Override
    public synchronized void testEnded(String testId, String testName) {
        offer(new EclipseTestEvent.TestEnded(testId, testName));
    }

    @Override
    public synchronized void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
        offer(new EclipseTestEvent.TestFailed(status, testId, testName, trace, expected, actual));
    }

    @Override
    public synchronized void testReran(String testId, String testClass, String testName, int status, String trace, String expected, String actual) {
        throw new UnsupportedOperationException("Unexpected call to testReran when running tests in Eclipse.");
    }

    private void offer(EclipseTestEvent event) {
        try {
            queue.offer(event, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // no interrupts expected
        }
    }
}
