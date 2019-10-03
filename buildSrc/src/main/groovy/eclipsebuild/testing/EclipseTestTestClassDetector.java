package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.detection.TestClassVisitor;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;

class EclipseTestTestClassDetector extends TestClassVisitor {

    EclipseTestTestClassDetector(final TestFrameworkDetector detector) {
        super(detector);
    }

    @Override
    protected boolean ignoreNonStaticInnerClass() {
        return true;
    }
}
