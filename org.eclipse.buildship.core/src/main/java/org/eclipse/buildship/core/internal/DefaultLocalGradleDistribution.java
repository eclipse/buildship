package org.eclipse.buildship.core.internal;

import java.io.File;

import org.eclipse.buildship.core.LocalGradleDistribution;

public final class DefaultLocalGradleDistribution extends DefaultGradleDistribution implements LocalGradleDistribution {

    public DefaultLocalGradleDistribution(File location) {
        this(location.getAbsolutePath());
    }

    public DefaultLocalGradleDistribution(String location) {
        super(new GradleDistributionInfo(Type.LOCAL_INSTALLATION, location));
    }

    @Override
    public File getLocation() {
        return new File(getDistributionInfo().getConfiguration());
    }
}
