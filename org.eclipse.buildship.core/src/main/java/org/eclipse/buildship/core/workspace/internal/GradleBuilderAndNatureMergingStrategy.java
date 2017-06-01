/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Algorithm for updating the Gradle natures and builders that considers the existing user setup.
 * <p/>
 * The algorithm will update existing elements with the new model such that only the managed
 * elements will be deleted. As of the ordering, the model elements are always first, followed by
 * the user-defined entries.
 *
 * @author Donat Csikos
 */
final class GradleBuilderAndNatureMergingStrategy {

    /**
     * Calculates the updated state.
     *
     * @param current elements currently defined on the project
     * @param model elements defined in the Gradle model
     * @param managed elements managed by Buildship
     * @return the description of the updated state
     */
    public static <T> Result<T> calculate(Set<T> current, Set<T> model, Set<T> managed) {
        Set<T> missing = Sets.newLinkedHashSet(current);
        missing.removeAll(model);

        Set<T> removed = Sets.newLinkedHashSet(missing);
        removed.retainAll(managed);

        Set<T> notRemoved = Sets.newHashSet(missing);
        notRemoved.removeAll(removed);

        Set<T> added = Sets.newLinkedHashSet(model);
        added.removeAll(current);

        Set<T> nextElements = Sets.newLinkedHashSet(model);
        nextElements.addAll(notRemoved);

        Set<T> nextManaged = Sets.newLinkedHashSet(managed);
        nextManaged.addAll(added);
        nextManaged.removeAll(removed);

        Result<T> result = new Result<T>(nextElements, nextManaged);
        return result;
    }

    /**
     * The description of the updated state.
     *
     * @param <T>
     */
    static class Result<T> {

        private final Set<T> nextElements;
        private final Set<T> newManaged;

        public Result(Set<T> nextElements, Set<T> nextManaged) {
            this.nextElements = nextElements;
            this.newManaged = nextManaged;
        }

        /**
         * @return the updated elements that should be set on the project
         */
        public Set<T> getNextElements() {
            return this.nextElements;
        }

        /**
         * @return the managed elements that can be freed if the Gradle model no longer contains it
         */
        public Set<T> getNextManaged() {
            return this.newManaged;
        }
    }
}
