/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import org.eclipse.buildship.core.WrapperGradleDistribution;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;

public final class DefaultWrapperGradleDistribution extends BaseGradleDistribution implements WrapperGradleDistribution {

    @Override
    public String toString() {
        return CoreMessages.GradleDistribution_Value_UseGradleWrapper;
    }
}
