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

package org.eclipse.buildship.core.internal.util.string;

import java.util.Deque;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Contains helper methods related to String  operations.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Removes adjacent duplicates.
     *
     * @param elements the elements to filter
     * @return the result with adjacent duplicates removed
     */
    public static ImmutableList<String> removeAdjacentDuplicates(List<String> elements) {
        Deque<String> result = Lists.newLinkedList();
        for (String element : elements) {
            if (result.isEmpty() || !result.getLast().equals(element)) {
                result.addLast(element);
            }
        }
        return ImmutableList.copyOf(result);
    }

    /**
     * Returns {@link Object#toString()} of the given value if the value is not null, otherwise null is returned.
     *
     * @param value the value to stringify
     * @return the string value or null
     */
    public static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

}
