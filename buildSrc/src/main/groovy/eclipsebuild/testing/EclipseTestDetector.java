package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.detection.AbstractTestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager;

class EclipseTestDetector extends AbstractTestFrameworkDetector<EclipseTestTestClassDetector> {
    EclipseTestDetector(ClassFileExtractionManager classFileExtractionManager) {
        super(classFileExtractionManager);
    }

    @Override
    protected EclipseTestTestClassDetector createClassVisitor() {
        return new EclipseTestTestClassDetector(this);
    }

    @Override
    protected boolean isKnownTestCaseClassName(String testCaseClassName) {
        return "spock/lang/Specification".equals(testCaseClassName);
    }
}

