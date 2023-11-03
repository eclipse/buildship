/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;

/**
 * A a reference to a specific version of Gradle. The appropriate distribution is downloaded and
 * installed into the user's Gradle home directory.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public final class FixedVersionGradleDistribution extends GradleDistribution {

    private final String version;

    FixedVersionGradleDistribution(String version) {
        this.version = Preconditions.checkNotNull(Strings.emptyToNull(version));
    }

    /**
     * The Gradle version to use.
     *
     * @return the Gradle version
     */
    public String getVersion() {
        return this.version;
    }

    @Override
    public void apply(GradleConnector connector) {
        connector.useGradleVersion(this.version);
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
        FixedVersionGradleDistribution other = (FixedVersionGradleDistribution) obj;
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
        return String.format("GRADLE_DISTRIBUTION(VERSION(%s))", this.version);
    }

    @Override
    public String getDisplayName() {
        return NLS.bind(CoreMessages.GradleDistribution_Value_UseGradleVersion_0, this.version);
    }
}
