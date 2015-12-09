package org.eclipse.buildship.core.launch;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.TestConfig.Builder;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * {@link TestTarget} implementation backed by an {@link IMethod} instance.
 */
public final class TestType implements TestTarget {

    private final IType type;

    private TestType(IType type) {
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public String getSimpleName() {
        return type.getElementName();
    }

    @Override
    public String getQualifiedName() {
        return type.getFullyQualifiedName();
    }

    @Override
    public void apply(Builder testConfig) {
        testConfig.jvmTestClasses(type.getFullyQualifiedName());
    }

    public static TestType from(IType type) {
        return new TestType(type);
    }
}