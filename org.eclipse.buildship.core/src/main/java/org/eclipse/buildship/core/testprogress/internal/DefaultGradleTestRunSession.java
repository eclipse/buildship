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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.TestDescriptor;
import org.gradle.tooling.TestFailedEvent;
import org.gradle.tooling.TestFailure;
import org.gradle.tooling.TestProgressEvent;
import org.gradle.tooling.TestSkippedEvent;
import org.gradle.tooling.TestStartedEvent;
import org.gradle.tooling.TestSucceededEvent;
import org.gradle.tooling.TestSuccess;
import org.gradle.tooling.TestSuiteFailedEvent;
import org.gradle.tooling.TestSuiteSkippedEvent;
import org.gradle.tooling.TestSuiteStartedEvent;
import org.gradle.tooling.TestSuiteSucceededEvent;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.model.ITestSessionListener;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestElement.Status;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.model.TestSuiteElement;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.testprogress.GradleTestRunSession;

/**
 * Default implementation of the {@code GradleTestRunSession} interface. Converts and delegates all received
 * test progress information in a format understood by the Eclipse Test Runner infrastructure.
 */
@SuppressWarnings("restriction")
public final class DefaultGradleTestRunSession extends TestRunSession implements GradleTestRunSession {

    private final TestSessionListenerContainer testSessionListeners;
    private final TestRunSessionStateTracker testSessionState;
    private final TestCounter testCounter;
    private final Map<TestDescriptor, TestSuiteElement> testSuites;
    private final Map<TestDescriptor, TestCaseElement> testCases;

    public DefaultGradleTestRunSession(ILaunch launch, IJavaProject project) {
        super(launch, project, 9999);
        this.testSessionListeners = new TestSessionListenerContainer();
        this.testSessionState = new TestRunSessionStateTracker();
        this.testCounter = new TestCounter();
        this.testSuites = Maps.newHashMap();
        this.testCases = Maps.newHashMap();
    }

    @Override
    public void start() {
        JUnitCorePlugin.getModel().addTestRunSession(this);
        JUnitCorePlugin.getModel().start();
        this.testSessionState.sessionStarted();
        this.testSessionListeners.notifySessionStarted();
        this.testSessionListeners.notifyTestingBegins();
    }

    @Override
    public void finish() {
        this.testSessionState.sessionFinished();
        this.testSessionListeners.notifySessionEnded(this.testSessionState.getDuration());
    }

    @Override
    public void process(TestProgressEvent event) {
        //CHECKSTYLE:OFF, required due to false negative in Checkstyle
        // suites
        if (event instanceof TestSuiteStartedEvent) {
            suiteStarted((TestSuiteStartedEvent) event);
        } else if (event instanceof TestSuiteSucceededEvent) {
            suiteSucceeded((TestSuiteSucceededEvent) event);
        } else if (event instanceof TestSuiteFailedEvent) {
            suiteFailed((TestSuiteFailedEvent) event);
        } else if (event instanceof TestSuiteSkippedEvent) {
            suiteSkipped((TestSuiteSkippedEvent) event);
        }

        // tests
        else if (event instanceof TestStartedEvent) {
            testStarted((TestStartedEvent) event);
        } else if (event instanceof TestSucceededEvent) {
            testSucceeded((TestSucceededEvent) event);
        } else if (event instanceof TestFailedEvent) {
            testFailed((TestFailedEvent) event);
        } else if (event instanceof TestSkippedEvent) {
            testSkipped((TestSkippedEvent) event);
        }
        //CHECKSTYLE:ON
    }

    private void suiteStarted(TestSuiteStartedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        Optional<TestDescriptor> parentCandidate = Optional.fromNullable(descriptor.getParent());
        TestSuiteElement parent = parentCandidate.isPresent() ? this.testSuites.get(parentCandidate.get()) : getTestRoot();
        TestSuiteElement testSuite = new TestSuiteElement(parent, String.valueOf(System.identityHashCode(descriptor)), descriptor.getName(), 0);
        testSuite.setStatus(Status.RUNNING);
        this.testSuites.put(descriptor, testSuite);
        this.testSessionListeners.notifySuiteStarted(testSuite);
    }

    private void suiteSucceeded(TestSuiteSucceededEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test suite %s.", descriptor.getName()));
        }

        TestSuccess result = event.getResult();
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testSuite.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testSuite.setStatus(Status.OK);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.OK, null, null, null);
    }

    private void suiteFailed(TestSuiteFailedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test suite %s.", descriptor.getName()));
        }

        TestFailure result = event.getResult();
        String trace = toString(result.getExceptions());
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testSuite.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testSuite.setStatus(Status.FAILURE, trace, null, null);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.FAILURE, trace, null, null);
    }

    private void suiteSkipped(TestSuiteSkippedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test suite %s.", descriptor.getName()));
        }

        testSuite.setStatus(Status.NOT_RUN);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.NOT_RUN, null, null, null);
    }

    private void testStarted(TestStartedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestSuiteElement parentSuite = this.testSuites.get(descriptor.getParent());
        if (parentSuite == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find parent for test %s.", descriptor.getName()));
        }

        TestCaseElement testCase = new TestCaseElement(parentSuite, String.valueOf(System.identityHashCode(descriptor)), descriptor.getName() + "(" + descriptor.getClassName() + ")");
        testCase.setStatus(Status.RUNNING);
        this.testCases.put(descriptor, testCase);
        this.testSessionListeners.notifyTestStarted(testCase);
        this.testCounter.incrementStarted();
    }

    private void testSucceeded(TestSucceededEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test %s.", descriptor.getName()));
        }

        TestSuccess result = event.getResult();
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testCase.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testCase.setStatus(Status.OK);
        this.testSessionListeners.notifyTestEnded(testCase);
        this.testCounter.incrementSuccess();
    }

    private void testFailed(TestFailedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test %s.", descriptor.getName()));
        }

        TestFailure result = event.getResult();
        String trace = toString(result.getExceptions());
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testCase.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testCase.setStatus(Status.FAILURE, trace, null, null);
        this.testSessionListeners.notifyTestEnded(testCase);
        this.testCounter.incrementFailure();
    }

    private void testSkipped(TestSkippedEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getDescriptor());

        TestDescriptor descriptor = event.getDescriptor();
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format("Cannot find test %s.", descriptor.getName()));
        }

        testCase.setStatus(Status.NOT_RUN);
        this.testSessionListeners.notifyTestEnded(testCase);
        this.testCounter.incrementIgnored();
    }

    @Override
    public void addTestSessionListener(ITestSessionListener listener) {
        // the constructor of the superclass tries to add something
        // at that time the listeners field is still null
        if (this.testSessionListeners != null) {
            this.testSessionListeners.add(listener);
        }
    }

    @Override
    public void removeTestSessionListener(ITestSessionListener listener) {
        this.testSessionListeners.remove(listener);
    }

    @Override
    public int getTotalCount() {
        // since tests are added dynamically while Gradle executes tests, the total number
        // of tests is the number of tests that have been started so far
        return this.testCounter.getStartedCount();
    }

    @Override
    public int getStartedCount() {
        // to have some progression in the ui, we display the number of started tasks as
        // the number of finished tasks
        return this.testCounter.getFinishedCount();
    }

    @Override
    public int getFailureCount() {
        return this.testCounter.getFailureCount();
    }

    @Override
    public int getErrorCount() {
        return this.testCounter.getErrorCount();
    }

    @Override
    public int getIgnoredCount() {
        return this.testCounter.getIgnoredCount();
    }

    @Override
    public boolean isRunning() {
        return this.testSessionState.isRunning();
    }

    @Override
    public boolean isStopped() {
        return this.testSessionState.isStopped();
    }

    @Override
    public synchronized void swapIn() {
    }

    @Override
    public void swapOut() {
    }

    private static String toString(List<Throwable> exceptions) {
        StringWriter stackTrace = new StringWriter();
        PrintWriter writer = new PrintWriter(stackTrace);
        for (Throwable t : exceptions) {
            t.printStackTrace(writer);
        }
        return stackTrace.toString();
    }

}
