package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.tasks.testing.Test;

public class EclipseTestTask extends Test {

    @Override
    protected JvmTestExecutionSpec createTestExecutionSpec() {
        return new EclipseTestExecutionSpec(super.createTestExecutionSpec(), this);
    }
}
