package org.eclipse.buildship.core.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.buildship.core.RemoteGradleDistribution;

public final class DefaultRemoteGradleDistribution extends DefaultGradleDistribution implements RemoteGradleDistribution {

    public DefaultRemoteGradleDistribution(URI url) {
        this(url.toString());
    }

    public DefaultRemoteGradleDistribution(String url) {
        super(new GradleDistributionInfo(Type.REMOTE_DISTRIBUTION, url));
    }

    @Override
    public URI getUrl() {
        try {
            return new URI(getDistributionInfo().getConfiguration());
        } catch (URISyntaxException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}
