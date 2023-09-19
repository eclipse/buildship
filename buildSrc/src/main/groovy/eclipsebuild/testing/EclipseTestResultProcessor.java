package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.*;
import org.gradle.api.internal.tasks.testing.results.AttachParentTestResultProcessor;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.api.tasks.testing.TestResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS;

public class EclipseTestResultProcessor {

    private final TestResultProcessor resultProcessor;
    private final String suiteName;
    private final Object testTaskOperationId;
    private final Object rootTestSuiteId;
    private final Logger logger;

    private TestDescriptorInternal currentTestSuite;
    private TestDescriptorInternal currentTestClass;
    private TestDescriptorInternal currentTestMethod;

    private Map<String, List<EclipseTestEvent.TestTreeEntry>> testTreeEntries = new HashMap<>();

    public EclipseTestResultProcessor(TestResultProcessor testResultProcessor, String suite, Object testTaskOperationId, Object rootTestSuiteId, Logger logger) {
        this.resultProcessor = new AttachParentTestResultProcessor(testResultProcessor);
        this.suiteName = suite;
        this.testTaskOperationId = testTaskOperationId;
        this.rootTestSuiteId = rootTestSuiteId;
        this.logger = logger;
    }

    public void onEvent(EclipseTestEvent event) {
        logger.debug("Eclipse test event: " + event);
        if (event instanceof EclipseTestEvent.TestTreeEntry) {
            onTestTreeEntryEvent((EclipseTestEvent.TestTreeEntry) event);
        } else if (event instanceof EclipseTestEvent.TestRunStarted) {
            onTestRunStartedEvent((EclipseTestEvent.TestRunStarted) event);
        } else if (event instanceof EclipseTestEvent.TestRunEnded) {
            onTestRunEndedEvent((EclipseTestEvent.TestRunEnded) event);
        } else if (event instanceof EclipseTestEvent.TestStarted) {
            onTestStartedEvent((EclipseTestEvent.TestStarted) event);
        } else if (event instanceof EclipseTestEvent.TestEnded) {
            onTestEndedEvent((EclipseTestEvent.TestEnded) event);
        } else if (event instanceof EclipseTestEvent.TestFailed) {
            onTestFailedEvent((EclipseTestEvent.TestFailed) event);
        } else {
            throw new RuntimeException("Unexpected event [type=" + event.getClass().getName() + "]: " + event);
        }
    }

    private void onTestTreeEntryEvent(EclipseTestEvent.TestTreeEntry event) {
        logger.info("Test tree entry (" + event.getTestId() + "): " + event.getFullFeatureQualifier());
        if (event.isSpock()) {
            testTreeEntries.getOrDefault(event.getFullFeatureQualifier(), new ArrayList<>()).add(event);
        }
    }

    private void onTestRunStartedEvent(EclipseTestEvent.TestRunStarted event) {
        this.currentTestSuite = testSuite(this.rootTestSuiteId, this.suiteName, this.testTaskOperationId);
        this.resultProcessor.started(this.currentTestSuite, startEvent());
    }

    private void onTestRunEndedEvent(EclipseTestEvent.TestRunEnded event) {
        if (this.currentTestClass!=null) {
            this.resultProcessor.completed(this.currentTestClass.getId(), completeEvent(SUCCESS));
        }
        this.resultProcessor.completed(this.currentTestSuite.getId(), completeEvent(SUCCESS));
    }

    private static final Pattern ECLIPSE_TEST_NAME = Pattern.compile("(.*)\\((.*)\\)");

    private void onTestStartedEvent(EclipseTestEvent.TestStarted event) {
        logger.info("Test started (" + event.getTestId() + "): " + event.getTestName());

        // TODO need idGenerator
        String testClass = event.getTestName();
        String testMethod = event.getTestName();
        Matcher matcher = ECLIPSE_TEST_NAME.matcher(event.getTestName());
        if (matcher.matches()) {
            testClass = matcher.group(2);
            testMethod = matcher.group(1);
        }

        String classId = getClassId(event);
        this.currentTestClass = testClass(classId, testClass, this.currentTestSuite);
        this.resultProcessor.started(this.currentTestClass, startEvent(this.currentTestSuite));
        this.currentTestMethod = testMethod(event.getTestId(), testClass, testMethod, this.currentTestClass);
        this.resultProcessor.started(this.currentTestMethod, startEvent(this.currentTestClass));
    }

    private static String getClassId(EclipseTestEvent.TestLifecycleCommon event) {
        String testId = event.getTestId();
        return getClassId(event.getTestId());
    }

    private static String getClassId(String testId) {
        return testId + " class";
    }

    private void onTestEndedEvent(EclipseTestEvent.TestEnded event) {
        String classId = getClassId(event);
        logger.info("Test completed (" + classId + "): " + event.getTestName());
        if (this.currentTestClass!=null) {
            this.currentTestClass = null;
        }
        this.resultProcessor.completed(classId, completeEvent(SUCCESS));
    }

    private void onTestFailedEvent(EclipseTestEvent.TestFailed event) {
        logger.info("Test failed (" + event.getTestId() + "): " + event.getTestName());
        String message = getFailureMessage(event);
        this.resultProcessor.output(this.currentTestMethod.getId(), new DefaultTestOutputEvent(TestOutputEvent.Destination.StdOut, message));
        this.resultProcessor.failure(this.currentTestMethod.getId(), new EclipseTestFailure(message, event.getTrace()));
    }

    private static String getFailureMessage(EclipseTestEvent.TestFailed event) {
        var message = new StringBuilder(event.getTestName()).append(" failed");
        if (event.getExpected()!=null || event.getActual()!=null) {
            message.append("expected=").append(event.getExpected()).append(", actual=").append(event.getActual()).append(")");
        }

        return message.append(". Stacktrace: ").append(event.getTrace()).toString();
    }

    private DefaultTestSuiteDescriptor testSuite(Object id, String displayName, final Object testTaskOperationid) {
        return new DefaultTestSuiteDescriptor(id, displayName) {
            private static final long serialVersionUID = 1L;
        };
    }

    private static DefaultTestClassDescriptor testClass(String id, String className, final TestDescriptorInternal parent) {
        return new DefaultTestClassDescriptor(id, className) {
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
