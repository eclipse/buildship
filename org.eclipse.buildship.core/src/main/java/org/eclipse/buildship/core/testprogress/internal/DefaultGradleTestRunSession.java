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
import java.util.Map;

import org.gradle.tooling.Failure;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestKind;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestFinishEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.TestProgressEvent;
import org.gradle.tooling.events.test.TestSkippedResult;
import org.gradle.tooling.events.test.TestStartEvent;
import org.gradle.tooling.events.test.TestSuccessResult;

import com.google.common.base.Optional;
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
import org.eclipse.buildship.core.i18n.CoreMessages;
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
    private final Map<OperationDescriptor, TestSuiteElement> testSuites;
    private final Map<OperationDescriptor, TestCaseElement> testCases;

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

        // we only handle JvmTestOperationDescriptor for now
        TestOperationDescriptor descriptor = event.getDescriptor();
        if (!(descriptor instanceof JvmTestOperationDescriptor)) {
            throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedDescriptorType + JvmTestOperationDescriptor.class);
        }
        JvmTestOperationDescriptor jvmDescriptor = (JvmTestOperationDescriptor) descriptor;
        JvmTestKind jvmTestKind = jvmDescriptor.getJvmTestKind();

        if (event instanceof TestStartEvent) {
            if (jvmTestKind == JvmTestKind.SUITE) {
                suiteStarted(jvmDescriptor);
            } else if (jvmTestKind == JvmTestKind.ATOMIC) {
                testStarted(jvmDescriptor);
            } else {
                throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedJVMKind + jvmTestKind);
            }
        } else if (event instanceof TestFinishEvent) {
            TestOperationResult result = ((TestFinishEvent) event).getResult();
            if (jvmTestKind == JvmTestKind.SUITE) {
                if (result instanceof TestSuccessResult) {
                    suiteSucceeded(jvmDescriptor, (TestSuccessResult) result);
                } else if (result instanceof TestFailureResult) {
                    suiteFailed(jvmDescriptor, (TestFailureResult) result);
                } else if (result instanceof TestSkippedResult) {
                    suiteSkipped(jvmDescriptor, (TestSkippedResult) result);
                } else {
                    throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedResultType + result.getClass());
                }
            } else if (jvmTestKind == JvmTestKind.ATOMIC) {
                if (result instanceof TestSuccessResult) {
                    testSucceeded(jvmDescriptor, (TestSuccessResult) result);
                } else if (result instanceof TestFailureResult) {
                    testFailed(jvmDescriptor, (TestFailureResult) result);
                } else if (result instanceof TestSkippedResult) {
                    testSkipped(jvmDescriptor, (TestSkippedResult) result);
                } else {
                    throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedResultType + result.getClass());
                }
            } else {
                throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedJVMKind + jvmTestKind);
            }
        } else {
            throw new IllegalArgumentException(CoreMessages.DefaultGradleTestRunSession_ErrorMessageUnsupportedEventType + event.getClass());
        }
        //CHECKSTYLE:ON
    }

    private void suiteStarted(JvmTestOperationDescriptor descriptor) {
        Optional<OperationDescriptor> parentCandidate = Optional.fromNullable(descriptor.getParent());
        TestSuiteElement parent = parentCandidate.isPresent() ? this.testSuites.get(parentCandidate.get()) : getTestRoot();
        TestSuiteElement testSuite = new TestSuiteElement(parent, String.valueOf(System.identityHashCode(descriptor)), descriptor.getName(), 0);
        testSuite.setStatus(Status.RUNNING);
        this.testSuites.put(descriptor, testSuite);
        this.testSessionListeners.notifySuiteStarted(testSuite);
    }

    private void suiteSucceeded(JvmTestOperationDescriptor descriptor, TestSuccessResult result) {
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTestSuite, descriptor.getName()));
        }
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testSuite.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testSuite.setStatus(Status.OK);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.OK, null, null, null);
    }

    private void suiteFailed(JvmTestOperationDescriptor descriptor, TestFailureResult result) {
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTestSuite, descriptor.getName()));
        }
        String trace = toString(result.getFailures());
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testSuite.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testSuite.setStatus(Status.FAILURE, trace, null, null);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.FAILURE, trace, null, null);
    }

    private void suiteSkipped(JvmTestOperationDescriptor descriptor, TestSkippedResult result) {
        TestSuiteElement testSuite = this.testSuites.get(descriptor);
        if (testSuite == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTestSuite, descriptor.getName()));
        }
        testSuite.setStatus(Status.NOT_RUN);
        this.testSessionListeners.notifySuiteFinished(testSuite, Status.NOT_RUN, null, null, null);
    }

    private void testStarted(JvmTestOperationDescriptor descriptor) {
        TestSuiteElement parentSuite = this.testSuites.get(descriptor.getParent());
        if (parentSuite == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindParent, descriptor.getName()));
        }
        TestCaseElement testCase = new TestCaseElement(parentSuite, String.valueOf(System.identityHashCode(descriptor)), descriptor.getName() + "(" + descriptor.getClassName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        testCase.setStatus(Status.RUNNING);
        this.testCases.put(descriptor, testCase);
        this.testSessionListeners.notifyTestStarted(testCase);
        this.testCounter.incrementStarted();
    }

    private void testSucceeded(JvmTestOperationDescriptor descriptor, TestSuccessResult result) {
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTest, descriptor.getName()));
        }
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testCase.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testCase.setStatus(Status.OK);
        this.testSessionListeners.notifyTestEnded(testCase);
        this.testCounter.incrementSuccess();
    }

    private void testFailed(JvmTestOperationDescriptor descriptor, TestFailureResult result) {
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTest, descriptor.getName()));
        }
        String trace = toString(result.getFailures());
        long elapsedTimeMilliseconds = result.getEndTime() - result.getStartTime();
        testCase.setElapsedTimeInSeconds(elapsedTimeMilliseconds / 1000d);
        testCase.setStatus(Status.FAILURE, trace, null, null);
        this.testSessionListeners.notifyTestEnded(testCase);
        this.testCounter.incrementFailure();
    }

    private void testSkipped(JvmTestOperationDescriptor descriptor, TestSkippedResult result) {
        TestCaseElement testCase = this.testCases.get(descriptor);
        if (testCase == null) {
            throw new GradlePluginsRuntimeException(String.format(CoreMessages.DefaultGradleTestRunSession_ErrorMessageCanNotFindTest, descriptor.getName()));
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

    private static String toString(List<? extends Failure> failures) {
        StringBuilder result = new StringBuilder();
        for (Failure failure : failures) {
            result.append(failure.getMessage());
            result.append('\n');
            result.append(failure.getDescription());
            result.append('\n');
        }
        return result.toString();
    }
}
