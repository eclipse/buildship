/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.GradleDistribution;

public abstract class BaseGradleDistribution implements GradleDistribution {

    protected void validate() {
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(this);
        Optional<String> validationError = distributionInfo.validate();
        Preconditions.checkArgument(!validationError.isPresent(), validationError.or(""));
    }
}
