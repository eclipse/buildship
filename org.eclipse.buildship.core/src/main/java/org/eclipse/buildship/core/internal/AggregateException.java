/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
