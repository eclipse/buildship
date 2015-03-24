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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.junit.model.ITestRunListener2;
import org.gradle.api.GradleException;
import org.gradle.api.internal.tasks.testing.DefaultTestClassDescriptor;
import org.gradle.api.internal.tasks.testing.DefaultTestMethodDescriptor;
import org.gradle.api.internal.tasks.testing.DefaultTestOutputEvent;
import org.gradle.api.internal.tasks.testing.DefaultTestSuiteDescriptor;
import org.gradle.api.internal.tasks.testing.TestCompleteEvent;
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.api.internal.tasks.testing.TestStartEvent;
import org.gradle.api.internal.tasks.testing.results.AttachParentTestResultProcessor;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.api.tasks.testing.TestResult;

public final class EclipseTestListener implements ITestRunListener2 {
    private static final Pattern ECLIPSE_TEST_NAME = Pattern.compile("(.*)\\((.*)\\)");

    /**
     * Test identifier prefix for ignored tests.
     */
    public static final String IGNORED_TEST_PREFIX = "@Ignore: "; //$NON-NLS-1$

    /**
     * Test identifier prefix for tests with assumption failures.
     */
    public static final String ASSUMPTION_FAILED_TEST_PREFIX = "@AssumptionFailure: "; //$NON-NLS-1$

    private final TestResultProcessor testResultProcessor;
    private final String suiteName;
    private final Object waitMonitor;
    private TestDescriptorInternal currentTestSuite;
    private TestDescriptorInternal currentTestClass;
    private TestDescriptorInternal currentTestMethod;
    private org.gradle.api.tasks.testing.TestResult.ResultType currentResult;

    public EclipseTestListener(TestResultProcessor testResultProcessor, String suite, Object waitMonitor) {
        this.testResultProcessor = new AttachParentTestResultProcessor(testResultProcessor);
        this.waitMonitor = waitMonitor;
        this.suiteName = suite;
    }

    @Override
    public synchronized void testRunStarted(int testCount) {
        this.currentTestSuite = new DefaultTestSuiteDescriptor("root", this.suiteName);
        this.testResultProcessor.started(this.currentTestSuite, new TestStartEvent(System.currentTimeMillis()));
    }

    @Override
    public synchronized void testRunEnded(long elapsedTime) {
        // System.out.println("Test Run Ended   - " + (failed() ? "FAILED" : "PASSED") +
        // " - Total: " + totalNumberOfTests
        // + " (Errors: " + numberOfTestsWithError
        // + ", Failed: " + numberOfTestsFailed
        // + ", Passed: " + numberOfTestsPassed + "), duration " + elapsedTime + "ms." + " id: " +
        // currentSuite.getId());

        this.testResultProcessor.completed(this.currentTestSuite.getId(), new TestCompleteEvent(System.currentTimeMillis()));
        synchronized (this.waitMonitor) {
            this.waitMonitor.notifyAll();
        }
    }

    @Override
    public synchronized void testRunStopped(long elapsedTime) {
        // System.out.println("Test Run Stopped");
        // TODO report failure when stopped?
        testRunEnded(elapsedTime);
    }

    @Override
    public synchronized void testRunTerminated() {
        // System.out.println("Test Run Terminated");
        // TODO report failure when terminated?
        testRunEnded(0);
    }

    @Override
    public synchronized void testStarted(String testId, String testName) {
        // TODO need idGenerator
        String testClass = testName;
        String testMethod = testName;
        Matcher matcher = ECLIPSE_TEST_NAME.matcher(testName);
        if (matcher.matches()) {
            testClass = matcher.group(2);
            testMethod = matcher.group(1);
        }

        this.currentTestClass = new DefaultTestClassDescriptor(testId + " class"/*
                                                                            * idGenerator.generateId(
                                                                            * )
                                                                            */, testClass);
        this.currentTestMethod = new DefaultTestMethodDescriptor(testId/* idGenerator.generateId() */, testClass, testMethod);
        this.currentResult = org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS;
        try {
            this.testResultProcessor.started(this.currentTestClass, new TestStartEvent(System.currentTimeMillis()));
            this.testResultProcessor.started(this.currentTestMethod, new TestStartEvent(System.currentTimeMillis()));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

    @Override
    public synchronized void testEnded(String testId, String testName) {
        if (testName.startsWith(IGNORED_TEST_PREFIX)) {
            if (this.currentResult == org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS) {
                this.currentResult = TestResult.ResultType.SKIPPED;
            } else {
                throw new GradleException("Failing ignored test is suspicious: " + testId + ", " + testName);
            }
        }
        this.testResultProcessor.completed(this.currentTestMethod.getId(), new TestCompleteEvent(System.currentTimeMillis(), this.currentResult));
        this.testResultProcessor.completed(this.currentTestClass.getId(), new TestCompleteEvent(System.currentTimeMillis()));
    }

    @Override
    public synchronized void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
        String statusMessage = String.valueOf(status);

        System.out.println("  Test - " + testName + " - status: " + statusMessage + ", trace: " + trace + ", expected: " + expected + ", actual: " + actual + " id: "
                + this.currentTestClass.getId());
        if (status == ITestRunListener2.STATUS_OK) {
            statusMessage = "OK";
            this.currentResult = org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS;
        } else if (status == ITestRunListener2.STATUS_FAILURE) {
            statusMessage = "FAILED";
            this.currentResult = org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE;
        } else if (status == ITestRunListener2.STATUS_ERROR) {
            statusMessage = "ERROR";
            this.currentResult = org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE;
        } else {
            throw new GradleException("Unknown status for test execution " + status + ", " + testId + ", " + testName);
        }

        if (this.currentTestMethod == null) {
            System.out.println("Test failure without current test method: " + testName + " - status: " + statusMessage + ", trace: " + trace + ", expected: " + expected
                    + ", actual: " + actual + " id: " + testId);
            return;
        }
        this.testResultProcessor.output(this.currentTestMethod.getId(), new DefaultTestOutputEvent(TestOutputEvent.Destination.StdOut, "Expected: " + expected + ", actual: " + actual));
        this.testResultProcessor.failure(this.currentTestMethod.getId(), new FailureThrowableStub(trace));
    }

    @Override
    public synchronized void testReran(String testId, String testClass, String testName, int status, String trace, String expected, String actual) {
        throw new UnsupportedOperationException("Unexpected call to testReran when running tests in Eclipse.");
    }

    @Override
    public synchronized void testTreeEntry(String description) {
        // System.out.println("Test Tree Entry - Description: " + description);
    }

    public static final class FailureThrowableStub extends Exception {

        private static final long serialVersionUID = 1L;
        private final String trace;

        public FailureThrowableStub(String trace) {
            super();
            this.trace = trace;
        }

        @Override
        public void printStackTrace(PrintStream s) {
            s.println(this.trace);
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            s.println(this.trace);
        }

    }

}
