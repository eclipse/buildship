package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.Test;

import java.util.Set;

public class EclipseTestTask extends Test {

    @Override
    protected JvmTestExecutionSpec createTestExecutionSpec() {
        Set<String> includePatterns = ((DefaultTestFilter)getFilter()).getCommandLineIncludePatterns();
        return new EclipseTestExecutionSpec(super.createTestExecutionSpec(), this, includePatterns);
    }
}
