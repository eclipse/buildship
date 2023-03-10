/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Exception that aggregates multiple cause exceptions.
 */
public final class AggregateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<Throwable> causes;

    public AggregateException(Collection<? extends Throwable> causes) {
        Preconditions.checkNotNull(causes);
        Preconditions.checkState(!causes.isEmpty());
        this.causes = ImmutableList.copyOf(causes);
    }

    public List<Throwable> getCauses() {
        return this.causes;
    }

}
