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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.gradle.api.tasks.testing.TestResult.ResultType;

import org.eclipse.jdt.internal.junit.model.ITestRunListener2;

public final class EclipseTestListener implements ITestRunListener2 {

    private static final Pattern ECLIPSE_TEST_NAME = Pattern.compile("(.*)\\((.*)\\)");

    private final TestResultProcessor resultProcessor;
    private final String suiteName;
    private final Object waitMonitor;
    private final Object testTaskOperationId;
    private final Object rootTestSuiteId;

    private TestDescriptorInternal currentTestSuite;
    private TestDescriptorInternal currentTestClass;
    private TestDescriptorInternal currentTestMethod;

    public EclipseTestListener(TestResultProcessor testResultProcessor, String suite, Object waitMonitor, Object testTaskOperationId, Object rootTestSuiteId) {
        this.resultProcessor = new AttachParentTestResultProcessor(testResultProcessor);
        this.waitMonitor = waitMonitor;
        this.suiteName = suite;
        this.testTaskOperationId = testTaskOperationId;
        this.rootTestSuiteId = rootTestSuiteId;
    }

    @Override
    public synchronized void testRunStarted(int testCount) {
        this.currentTestSuite = testSuite(this.rootTestSuiteId, this.suiteName, this.testTaskOperationId);
        this.resultProcessor.started(this.currentTestSuite, startEvent());
    }

    @Override
    public synchronized void testRunEnded(long elapsedTime) {
        if (this.currentTestClass != null) {
            this.resultProcessor.completed(this.currentTestClass.getId(), completeEvent(ResultType.SUCCESS));
        }

        this.resultProcessor.completed(this.currentTestSuite.getId(), completeEvent(ResultType.SUCCESS));
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

        String classId = testId + " class";
        if (this.currentTestClass == null) {
            this.currentTestClass = testClass(classId, testClass, this.currentTestSuite);
            this.resultProcessor.started(this.currentTestClass, startEvent(this.currentTestSuite));
        } else if (!this.currentTestClass.getId().equals(classId)) {
            this.resultProcessor.completed(this.currentTestClass.getId(), completeEvent(ResultType.SUCCESS));
            this.currentTestClass = testClass(classId, testClass, this.currentTestSuite);
            this.resultProcessor.started(this.currentTestClass, startEvent(this.currentTestSuite));
        }

        this.currentTestMethod = testMethod(testId, testClass, testMethod, this.currentTestClass);
        this.resultProcessor.started(this.currentTestMethod, startEvent(this.currentTestClass));
    }

    @Override
    public synchronized void testEnded(String testId, String testName) {
        this.resultProcessor.completed(testId, completeEvent(ResultType.SUCCESS));
    }

    @Override
    public synchronized void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
        String message = testName + " failed";
        if (expected != null || actual != null) {
            message += " (expected=" + expected + ", actual=" + actual + ")";
        }

        this.resultProcessor.output(this.currentTestMethod.getId(), new DefaultTestOutputEvent(TestOutputEvent.Destination.StdOut, message));
        this.resultProcessor.failure(this.currentTestMethod.getId(), new EclipseTestFailure(message, trace));
    }

    @Override
    public synchronized void testReran(String testId, String testClass, String testName, int status, String trace, String expected, String actual) {
        throw new UnsupportedOperationException("Unexpected call to testReran when running tests in Eclipse.");
    }

    @Override
    public synchronized void testTreeEntry(String description) {
    }

    private DefaultTestSuiteDescriptor testSuite(Object id, String displayName, final Object testTaskOperationid) {
        return new DefaultTestSuiteDescriptor(id, displayName) {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getOwnerBuildOperationId() {
                return testTaskOperationid;
            }
        };
    }

    private static DefaultTestClassDescriptor testClass(String id, String className, final TestDescriptorInternal parent) {
        return new DefaultTestClassDescriptor(id, className){
            private static final long serialVersionUID = 1L;

            @Override
            public TestDescriptorInternal getParent() {
                return parent;
            }

        };
    }

    private static DefaultTestMethodDescriptor testMethod(String id, String className, String methodName, final TestDescriptorInternal parent) {
        return new DefaultTestMethodDescriptor(id, className, methodName) {
            private static final long serialVersionUID = 1L;

            @Override
            public TestDescriptorInternal getParent() {
                return parent;
            }
        };
    }

    private static TestStartEvent startEvent() {
        return new TestStartEvent(System.currentTimeMillis());
    }

    private static TestStartEvent startEvent(TestDescriptorInternal parent) {
        return new TestStartEvent(System.currentTimeMillis(), parent.getId());
    }

    private static TestCompleteEvent completeEvent(ResultType resultType) {
        return new TestCompleteEvent(System.currentTimeMillis(), resultType);
    }

    public static void main(String[] args) {
        System.out.println(new RuntimeException("asdfasdf").toString());
    }

}
