/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.buildship.core.RemoteGradleDistribution;

public final class DefaultRemoteGradleDistribution extends BaseGradleDistribution implements RemoteGradleDistribution {

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
