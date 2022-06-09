package eclipsebuild.testing;

interface EclipseTestEvent {
    class TestRunStarted implements EclipseTestEvent {
    }

    class TestRunEnded implements EclipseTestEvent {
    }

    class TestStarted implements EclipseTestEvent {

        private final String testId;
        private final String testName;

        public TestStarted(String testId, String testName) {
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

    class TestEnded implements EclipseTestEvent {

        private final String testId;
        private final String testName;

        public TestEnded(String testId, String testName) {
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

    class TestFailed implements EclipseTestEvent {
        private final int status;
        private final String testId;
        private final String testName;
        private final String trace;
        private final String expected;
        private final String actual;

        public TestFailed(int status, String testId, String testName, String trace, String expected, String actual) {
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
}
