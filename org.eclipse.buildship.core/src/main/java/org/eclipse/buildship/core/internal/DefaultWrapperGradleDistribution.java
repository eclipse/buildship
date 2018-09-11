package org.eclipse.buildship.core.internal;

import org.eclipse.buildship.core.WrapperGradleDistribution;

public final class DefaultWrapperGradleDistribution extends DefaultGradleDistribution implements WrapperGradleDistribution {

    protected DefaultWrapperGradleDistribution() {
        super(new GradleDistributionInfo(Type.WRAPPER, null));
    }
}
