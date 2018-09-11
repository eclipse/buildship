/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.io.File;

import com.google.common.base.Preconditions;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.LocalGradleDistribution;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;

public final class DefaultLocalGradleDistribution extends BaseGradleDistribution implements LocalGradleDistribution {

    private final File location;

    public DefaultLocalGradleDistribution(File location) {
        this.location = Preconditions.checkNotNull(location);
        validate();
    }

    @Override
    public File getLocation() {
        return this.location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.location == null) ? 0 : this.location.hashCode());
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
        DefaultLocalGradleDistribution other = (DefaultLocalGradleDistribution) obj;
        if (this.location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!this.location.equals(other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return NLS.bind(CoreMessages.GradleDistribution_Value_UseLocalInstallation_0, this.location.getAbsolutePath());
    }
}
