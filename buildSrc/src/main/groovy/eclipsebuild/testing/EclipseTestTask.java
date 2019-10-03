package eclipsebuild.testing;

import groovy.lang.Closure;
import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.tasks.testing.Test;

public class EclipseTestTask extends Test {

    TestFramework framework = new EclipseTestFramework(this, (DefaultTestFilter) getFilter(), getInstantiator(), getClassLoaderCache());

    @Override
    protected JvmTestExecutionSpec createTestExecutionSpec() {
        JvmTestExecutionSpec execSpec = super.createTestExecutionSpec();
        return new EclipseTestExecutionSpec(execSpec, framework, this);
    }

    @Override
    public TestFramework getTestFramework() {
        return framework;
    }

    @Override
    public TestFramework testFramework(Closure testFrameworkConfigure) {
        return framework;
    }
}
