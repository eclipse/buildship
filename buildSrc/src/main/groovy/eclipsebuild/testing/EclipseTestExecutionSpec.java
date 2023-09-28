package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.tasks.testing.Test;

import java.util.Set;

public class EclipseTestExecutionSpec extends JvmTestExecutionSpec {

    private final Test testTask;
    Set<String> includePatterns;

    public EclipseTestExecutionSpec(JvmTestExecutionSpec spec, Test testTask, Set<String> includePatterns) {
        super(spec.getTestFramework(), spec.getClasspath(), spec.getCandidateClassFiles(), spec.isScanForTestClasses(), spec.getTestClassesDirs(), spec.getPath(), spec.getIdentityPath(), spec.getForkEvery(), spec.getJavaForkOptions(), spec.getMaxParallelForks(), spec.getPreviousFailedTestClasses());
        this.testTask = testTask;
        this.includePatterns = includePatterns;
    }

    public Test getTestTask() {
        return testTask;
    }

    public Set<String> getFilters() {
        return includePatterns;
    }
}
