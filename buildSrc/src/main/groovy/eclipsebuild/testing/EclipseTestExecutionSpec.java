package eclipsebuild.testing;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.internal.tasks.testing.TestExecutionSpec;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.tasks.testing.Test;
import org.gradle.process.JavaForkOptions;
import org.gradle.util.Path;

import java.io.File;
import java.util.Set;

public class EclipseTestExecutionSpec extends JvmTestExecutionSpec {

    private final Test testTask;

    public EclipseTestExecutionSpec(JvmTestExecutionSpec spec, Test testTask) {
        super(spec.getTestFramework(), spec.getClasspath(), spec.getCandidateClassFiles(), spec.isScanForTestClasses(), spec.getTestClassesDirs(), spec.getPath(), spec.getIdentityPath(), spec.getForkEvery(), spec.getJavaForkOptions(), spec.getMaxParallelForks(), spec.getPreviousFailedTestClasses());
        this.testTask = testTask;
    }

    public Test getTestTask() {
        return testTask;
    }
}
