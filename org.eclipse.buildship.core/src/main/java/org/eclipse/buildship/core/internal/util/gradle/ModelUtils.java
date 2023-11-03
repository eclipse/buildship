/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gradle.tooling.model.DomainObjectSet;

/**
 * Contains helper methods related to the Tooling API models.
 *
 * @author Donat Csikos
 */
public final class ModelUtils {

    private ModelUtils() {
    }

    static <T> DomainObjectSet<? extends T> asDomainObjectSet(Iterable<? extends T> result) {
        return ImmutableDomainObjectSet.of(result);
    }

    static <T> DomainObjectSet<? extends T> emptyDomainObjectSet() {
        return ImmutableDomainObjectSet.of(Collections.<T> emptyList());
    }

    /**
     * Default implementation of the {@link DomainObjectSet} interface.
     *
     * This class copied from Gradle Core 4.6 as it is not part of the public API.
     * Source URL: https://github.com/gradle/gradle/blob/v4.6.0/subprojects/tooling-api/src/main/java/org/gradle/tooling/model/internal/ImmutableDomainObjectSet.java
     *
     * @param <T> the type of the contained elements
     */
    private static class ImmutableDomainObjectSet<T> extends AbstractSet<T> implements DomainObjectSet<T>, Serializable {
        private static final long serialVersionUID = 1L;

        private final Set<T> elements = new LinkedHashSet<>();

        public ImmutableDomainObjectSet(Iterable<? extends T> elements) {
            for (T element : elements) {
                this.elements.add(element);
            }
        }

        @Override
        public Iterator<T> iterator() {
            return this.elements.iterator();
        }

        @Override
        public int size() {
            return this.elements.size();
        }

        @Override
        public T getAt(int index) throws IndexOutOfBoundsException {
            return getAll().get(index);
        }

        @Override
        public List<T> getAll() {
            return new ArrayList<>(this.elements);
        }

        public static <T> ImmutableDomainObjectSet<T> of(Iterable<? extends T> elements) {
            return new ImmutableDomainObjectSet<>(elements);
        }
    }
}
