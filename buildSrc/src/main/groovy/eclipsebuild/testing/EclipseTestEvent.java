package eclipsebuild.testing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
interface EclipseTestEvent {

    class TestTreeEntry extends TestLifecycleCommon {

// 2,GradleImportTaskTest,true,5,false,1,GradleImportTaskTest,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]
// 3,Imports project into workspace,false,1,false,2,Imports project into workspace,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_0]
// 4,Manual trigger causes synchronization,false,1,false,2,Manual trigger causes synchronization,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_1]
// 5,Startup trigger does not synchronize existing projects,false,1,false,2,Startup trigger does not synchronize existing projects,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_2]
// 6,Startup trigger imports projects that are not already in the workspace,false,1,false,2,Startup trigger imports projects that are not already in the workspace,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_3]
// 7,new build configuration can override workspace settings,false,1,false,2,new build configuration can override workspace settings,,[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_4]
// 11,
// new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(/Users/reinholddegenfellner/.gradle/wrapper/dists/gradle-5.4.1-bin/e75iq110yv9r9wt1a6619x2xm/gradle-5.4.1))\, distributionType: LOCAL_INSTALLATION\, offlineMode: false\, buildScansEnabled: true\, autoSync: false\, showConsole: true\, showExecutions: false\, customGradleHome: true\, #3],
// false,1,true,7,
// new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(/Users/reinholddegenfellner/.gradle/wrapper/dists/gradle-5.4.1-bin/e75iq110yv9r9wt1a6619x2xm/gradle-5.4.1))\, distributionType: LOCAL_INSTALLATION\, offlineMode: false\, buildScansEnabled: true\, autoSync: false\, showConsole: true\, showExecutions: false\, customGradleHome: true\, #3],,
// [engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]/[feature:$spock_feature_2_4]/[iteration:3]

        private static final Pattern ENGINE_SPOCK = Pattern.compile(".*\\[engine:spock\\]\\/\\[spec:([^\\]]+)\\](\\/\\[feature:([^\\]]+)\\])?\\/?(\\[iteration:([0-9]+)\\])?");
        private static final Pattern TREE_ENTRY = Pattern.compile("(\\w+),(.*[^\\\\]),(true|false),([0-9]+).*");
        private final boolean isSpock;
        private String spockSpec;
        private String spockFeature;
        private int testCount;
        private boolean isSuite = false;
        private String spockIteration = null;

        public TestTreeEntry(String description){
            super(null, null);
            Matcher spockMatcher = ENGINE_SPOCK.matcher(description);
            this.isSpock = spockMatcher.matches();
            if(isSpock()){
                this.spockIteration = spockMatcher.group(5);
                this.spockFeature = spockMatcher.group(3);
                this.spockSpec = spockMatcher.group(1);
            }
            Matcher treeEntryMatch = TREE_ENTRY.matcher(description);
            if (treeEntryMatch.matches()) {
                this.testId = treeEntryMatch.group(1);
                this.testName = treeEntryMatch.group(2);
                this.isSuite = Boolean.parseBoolean(treeEntryMatch.group(3));
                this.testCount = Integer.parseInt(treeEntryMatch.group(4));
            }
        }

        public boolean isSpock() {
            return isSpock;
        }

        public String getSpockIteration() {
            return spockIteration;
        }

        public String getSpockSpec() {
            return spockSpec;
        }

        public String getSpockFeature() {
            return spockFeature;
        }

        public String getFullFeatureQualifier(){
            return spockSpec + "/" + spockFeature;
        }
    }

    class TestRunStarted implements EclipseTestEvent {
        final int count;

        TestRunStarted(int count){
            this.count = count;
        }

        public String toString() {
            return count + " " + super.toString();
        }
    }

    class TestRunEnded implements EclipseTestEvent {
    }

    abstract class TestLifecycleCommon implements EclipseTestEvent {

        protected String testId;
        protected String testName;

        public TestLifecycleCommon(String testId, String testName) {
            this.testId = testId;
            this.testName = testName;
        }

        public String getTestId() {
            return testId;// + " " + testName.hashCode();
        }

        public String getTestName() {
            return testName;
        }

        public String toString() {
            return testId + ":" + testName;
        }
    }

    class TestStarted extends TestLifecycleCommon {

        public TestStarted(String testId, String testName) {
            super(testId, testName);
        }
    }

    class TestEnded extends TestLifecycleCommon  {
        public TestEnded(String testId, String testName) {
            super(testId, testName);
        }
    }

    class TestFailed extends TestLifecycleCommon {
        private final int status;
        private final String trace;
        private final String expected;
        private final String actual;

        public TestFailed(int status, String testId, String testName, String trace, String expected, String actual) {
            super(testId, testName);
            this.status = status;
            this.trace = trace;
            this.expected = expected;
            this.actual = actual;
        }

        public int getStatus() {
            return status;
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
