package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.detection.AbstractTestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager;

class EclipseTestFrameworkDetector extends AbstractTestFrameworkDetector<EclipseTestClassDetector> {
    EclipseTestFrameworkDetector(ClassFileExtractionManager classFileExtractionManager) {
        super(classFileExtractionManager);
    }

    @Override
    protected EclipseTestClassDetector createClassVisitor() {
        return new EclipseTestClassDetector(this);
    }

    @Override
    protected boolean isKnownTestCaseClassName(String testCaseClassName) {
        return "spock/lang/Specification".equals(testCaseClassName);
    }
}
