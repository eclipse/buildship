package org.eclipse.buildship.core;

import java.net.URI;

public interface RemoteGradleDistribution extends GradleDistribution {

    URI getUrl();
}
