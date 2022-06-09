package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.*;
import org.gradle.api.internal.tasks.testing.results.AttachParentTestResultProcessor;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.api.tasks.testing.TestResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EclipseTestResultProcessor {

    private static final Pattern ECLIPSE_TEST_NAME = Pattern.compile("(.*)\\((.*)\\)");

    private final TestResultProcessor resultProcessor;
    private final String suiteName;
    private final Object testTaskOperationId;
    private final Object rootTestSuiteId;
    private final Logger logger;

    private TestDescriptorInternal currentTestSuite;
    private TestDescriptorInternal currentTestClass;
    private TestDescriptorInternal currentTestMethod;

    public EclipseTestResultProcessor(TestResultProcessor testResultProcessor, String suite, Object testTaskOperationId, Object rootTestSuiteId, Logger logger) {
        this.resultProcessor = new AttachParentTestResultProcessor(testResultProcessor);
        this.suiteName = suite;
        this.testTaskOperationId = testTaskOperationId;
        this.rootTestSuiteId = rootTestSuiteId;
        this.logger = logger;
    }

    public void onEvent(EclipseTestListener.EclipseTestEvent event) {
        if (event instanceof EclipseTestListener.TestRunStartedEvent) {
            onTestRunStartedEvent((EclipseTestListener.TestRunStartedEvent) event);
        } else if (event instanceof EclipseTestListener.TestRunEndedEvent){
            onTestRunEndedEvent((EclipseTestListener.TestRunEndedEvent) event);
        } else if (event instanceof EclipseTestListener.TestStartedEvent){
            onTestStartedEvent((EclipseTestListener.TestStartedEvent) event);
        } else if (event instanceof EclipseTestListener.TestEndedEvent){
            onTestEndedEvent((EclipseTestListener.TestEndedEvent) event);
        } else if (event instanceof EclipseTestListener.TestFailedEvent){
            onTestFailedEvent((EclipseTestListener.TestFailedEvent) event);
        } else {
            throw new RuntimeException("Unexpected event [type=" + event.getClass().getName() + "]: " + event);
        }
    }

    private void onTestRunStartedEvent(EclipseTestListener.TestRunStartedEvent event) {
        this.currentTestSuite = testSuite(this.rootTestSuiteId, this.suiteName, this.testTaskOperationId);
        this.resultProcessor.started(this.currentTestSuite, startEvent());
    }

    private void onTestRunEndedEvent(EclipseTestListener.TestRunEndedEvent event) {
        if (this.currentTestClass != null) {
            this.resultProcessor.completed(this.currentTestClass.getId(), completeEvent(TestResult.ResultType.SUCCESS));
        }
        this.resultProcessor.completed(this.currentTestSuite.getId(), completeEvent(TestResult.ResultType.SUCCESS));
    }

    private void onTestStartedEvent(EclipseTestListener.TestStartedEvent event) {
        logger.info("Test started: " + event.getTestName());

        // TODO need idGenerator
        String testClass = event.getTestName();
        String testMethod = event.getTestName();
        Matcher matcher = ECLIPSE_TEST_NAME.matcher(event.getTestName());
        if (matcher.matches()) {
            testClass = matcher.group(2);
            testMethod = matcher.group(1);
        }

        String classId = event.getTestId() + " class";
        if (this.currentTestClass == null) {
            this.currentTestClass = testClass(classId, testClass, this.currentTestSuite);
            this.resultProcessor.started(this.currentTestClass, startEvent(this.currentTestSuite));
        } else if (!this.currentTestClass.getId().equals(classId)) {
            this.resultProcessor.completed(this.currentTestClass.getId(), completeEvent(TestResult.ResultType.SUCCESS));
            this.currentTestClass = testClass(classId, testClass, this.currentTestSuite);
            this.resultProcessor.started(this.currentTestClass, startEvent(this.currentTestSuite));
        }

        this.currentTestMethod = testMethod(event.getTestId(), testClass, testMethod, this.currentTestClass);
        this.resultProcessor.started(this.currentTestMethod, startEvent(this.currentTestClass));
    }

    private void onTestEndedEvent(EclipseTestListener.TestEndedEvent event) {
        this.resultProcessor.completed(event.getTestId(), completeEvent(TestResult.ResultType.SUCCESS));
    }

    private void onTestFailedEvent(EclipseTestListener.TestFailedEvent event) {
        String message = event.getTestName() + " failed";
        if (event.getExpected() != null || event.getActual() != null) {
            message += " (expected=" + event.getExpected() + ", actual=" + event.getActual() + ")";
        }
        this.resultProcessor.output(this.currentTestMethod.getId(), new DefaultTestOutputEvent(TestOutputEvent.Destination.StdOut, message));
        this.resultProcessor.failure(this.currentTestMethod.getId(), new EclipseTestFailure(message, event.getTrace()));
    }

    private DefaultTestSuiteDescriptor testSuite(Object id, String displayName, final Object testTaskOperationid) {
        return new DefaultTestSuiteDescriptor(id, displayName) {
            private static final long serialVersionUID = 1L;
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

    private static TestCompleteEvent completeEvent(TestResult.ResultType resultType) {
        return new TestCompleteEvent(System.currentTimeMillis(), resultType);
    }
}
