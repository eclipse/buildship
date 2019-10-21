package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.tasks.testing.Test;

public class EclipseTestExecutionSpec extends JvmTestExecutionSpec {

    private final Test testTask;

    public EclipseTestExecutionSpec(JvmTestExecutionSpec spec, TestFramework framework, Test testTask) {
        super(framework, spec.getClasspath(), spec.getCandidateClassFiles(), spec.isScanForTestClasses(), spec.getTestClassesDirs(), spec.getPath(), spec.getIdentityPath(), spec.getForkEvery(), spec.getJavaForkOptions(), spec.getMaxParallelForks(), spec.getPreviousFailedTestClasses());
        this.testTask = testTask;
    }

    public Test getTestTask() {
        return testTask;
    }
}
