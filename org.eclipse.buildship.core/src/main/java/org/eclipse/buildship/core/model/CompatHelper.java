/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import java.util.Collections;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

/**
 * Contains utility functions for TAPI models.
 * @author Donat Csikos
 */
final class CompatHelper {


    private CompatHelper() {
    }

    static <T> DomainObjectSet<? extends T> asDomainSet(Iterable<? extends T> result) {
        return ImmutableDomainObjectSet.of(result);
    }

    static <T> DomainObjectSet<? extends T> emptyDomainSet() {
        return ImmutableDomainObjectSet.of(Collections.<T> emptyList());
    }
}
