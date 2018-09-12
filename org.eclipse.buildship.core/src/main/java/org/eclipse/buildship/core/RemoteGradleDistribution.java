/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core;

import java.net.URI;

import com.google.common.base.Preconditions;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;

/**
 * A reference to a remote Gradle distribution.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public final class RemoteGradleDistribution implements GradleDistribution {

    private final URI url;

    RemoteGradleDistribution(URI url) {
        this.url = Preconditions.checkNotNull(url);
    }

    /**
     * The URL pointing to the the remote Gradle distribution.
     *
     * @return the remote distribution location
     */
    public URI getUrl() {
        return this.url;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RemoteGradleDistribution other = (RemoteGradleDistribution) obj;
        if (this.url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!this.url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return NLS.bind(CoreMessages.GradleDistribution_Value_UseRemoteDistribution_0, this.url.toString());
    }
}
