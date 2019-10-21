package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.detection.TestClassVisitor;

class EclipseTestClassDetector extends TestClassVisitor {

    EclipseTestClassDetector(final EclipseTestFrameworkDetector detector) {
        super(detector);
    }

    @Override
    protected boolean ignoreNonStaticInnerClass() {
        return true;
    }
}
