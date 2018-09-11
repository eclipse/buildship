/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;

public final class DefaultFixedVersionGradleDistribution extends BaseGradleDistribution implements FixedVersionGradleDistribution {

    private final String version;

    public DefaultFixedVersionGradleDistribution(String version) {
        super(new GradleDistributionInfo(GradleDistributionInfo.Type.VERSION, version));
        this.version = Preconditions.checkNotNull(version);
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
