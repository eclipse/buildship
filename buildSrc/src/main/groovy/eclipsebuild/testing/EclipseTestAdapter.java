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
import org.gradle.api.internal.tasks.testing.DefaultTestClassDescriptor;
import org.gradle.api.internal.tasks.testing.DefaultTestDescriptor;
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
import org.gradle.api.tasks.testing.TestResult.ResultType;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.time.Clock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EclipseTestAdapter implements ITestRunListener2 {

    private final TestResultProcessor resultProcessor;
    private final Object rootTestSuiteId;
    private final Clock clock;
    private final IdGenerator<?> idGenerator;
    private final Object lock = new Object();
    private final Map<String, TestDescriptorInternal> executing = new HashMap<String, TestDescriptorInternal>();


    public EclipseTestAdapter(TestResultProcessor resultProcessor, Object rootTestSuiteId, Clock clock, IdGenerator<?> idGenerator) {
        this.resultProcessor = resultProcessor;
        this.rootTestSuiteId = rootTestSuiteId;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Override
    public synchronized void testRunStarted(int testCount) {
    }

    @Override
    public synchronized void testRunEnded(long elapsedTime) {
    }

    @Override
    public synchronized void testRunStopped(long elapsedTime) {
    }

    @Override
    public synchronized void testRunTerminated() {
    }

    @Override
    public synchronized void testStarted(String testId, String testName) {
        TestDescriptorInternal descriptor = nullSafeDescriptor(idGenerator.generateId(), testName);
        synchronized (lock) {
            TestDescriptorInternal oldTest = executing.put(testId, descriptor);
            assert oldTest == null : String.format("Unexpected start event for %s", testName);
        }
        resultProcessor.started(descriptor, startEvent());
    }

    @Override
    public synchronized void testEnded(String testId, String testName) {
        long endTime = clock.getCurrentTime();
        TestDescriptorInternal testInternal;
        ResultType resultType = ResultType.SUCCESS;
        synchronized (lock) {
            testInternal = executing.remove(testId);
            if (testInternal == null && executing.size() == 1) {
                // Assume that test has renamed itself (this can actually happen)
                testInternal = executing.values().iterator().next();
                executing.clear();
            }
            assert testInternal != null : String.format("Unexpected end event for %s", testName);
            resultType = null;
        }
        resultProcessor.completed(testInternal.getId(), new TestCompleteEvent(endTime, resultType));
    }

    @Override
    public synchronized void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
        TestDescriptorInternal descriptor = nullSafeDescriptor(idGenerator.generateId(), testName);
        TestDescriptorInternal testInternal;
        synchronized (lock) {
            testInternal = executing.get(testId);
        }
        boolean needEndEvent = false;
        if (testInternal == null) {
            // This can happen when, for example, a @BeforeClass or @AfterClass method fails
            needEndEvent = true;
            testInternal = descriptor;
            resultProcessor.started(testInternal, startEvent());
        }
        String message = testName + " failed";
        if (expected != null || actual != null) {
            message += " (expected=" + expected + ", actual=" + actual + ")";
        }
        resultProcessor.failure(testInternal.getId(),  new EclipseTestFailure(message, trace));
        if (needEndEvent) {
            resultProcessor.completed(testInternal.getId(), new TestCompleteEvent(clock.getCurrentTime()));
        }
    }

    @Override
    public synchronized void testReran(String testId, String testClass, String testName, int status, String trace, String expected, String actual) {
    }

    @Override
    public synchronized void testTreeEntry(String description) {
    }

    private TestDescriptorInternal nullSafeDescriptor(Object id, String testName) {
        String methodName = methodName(testName);
        if (methodName != null) {
            return new DefaultTestDescriptor(id, className(testName), methodName);
        } else {
            return new DefaultTestDescriptor(id, className(testName), "classMethod");
        }
    }

    private static String className(String testName) {
        return testName.substring(testName.lastIndexOf('(') + 1, testName.length() - 1);
    }

    private static String methodName(String testName) {
        return testName.substring(0, testName.lastIndexOf('('));
    }

    private TestStartEvent startEvent() {
        return new TestStartEvent(clock.getCurrentTime());
    }
}
