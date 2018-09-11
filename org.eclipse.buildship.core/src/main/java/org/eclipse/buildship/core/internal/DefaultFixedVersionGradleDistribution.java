package org.eclipse.buildship.core.internal;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;

public final class DefaultFixedVersionGradleDistribution extends DefaultGradleDistribution implements FixedVersionGradleDistribution {

    public DefaultFixedVersionGradleDistribution(String version) {
        super(new GradleDistributionInfo(Type.VERSION, version));
    }

    @Override
    public String getVersion() {
        return getDistributionInfo().getConfiguration();
    }
}
