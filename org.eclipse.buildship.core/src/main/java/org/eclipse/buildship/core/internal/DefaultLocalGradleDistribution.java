/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
