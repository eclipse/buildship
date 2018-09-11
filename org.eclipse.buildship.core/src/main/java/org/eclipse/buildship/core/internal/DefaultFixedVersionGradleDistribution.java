/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import com.google.common.base.Preconditions;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;

public final class DefaultFixedVersionGradleDistribution extends BaseGradleDistribution implements FixedVersionGradleDistribution {

    private final String version;

    public DefaultFixedVersionGradleDistribution(String version) {
        this.version = Preconditions.checkNotNull(version);
        validate();
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
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
        DefaultFixedVersionGradleDistribution other = (DefaultFixedVersionGradleDistribution) obj;
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return NLS.bind(CoreMessages.GradleDistribution_Value_UseGradleVersion_0, this.version);
    }
}
