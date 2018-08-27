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

package org.eclipse.buildship.core.internal.util.collections;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Contains helper methods related to Collections operations.
 */
public final class CollectionsUtils {

    private static final char SPACE = ' ';
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private CollectionsUtils() {
    }

    /**
     * Splits the given string for each space that is found.
     *
     * @param string the string to split, can be null
     * @return the split string with each segment being an element of the returned list, never null
     */
    public static ImmutableList<String> splitBySpace(String string) {
        return Strings.isNullOrEmpty(string) ? ImmutableList.<String>of() : ImmutableList.copyOf(Splitter.on(SPACE).omitEmptyStrings().splitToList(string));
    }

    /**
     * Splits the given string for each space that is found.
     *
     * @param elements the elements to join, must not be null
     * @return the joined elements returned as a single string with each element separated by a
     * space
     */
    public static String joinWithSpace(List<String> elements) {
        return Joiner.on(SPACE).join(elements);
    }

    /**
     * Returns the given array if it is non-null, the empty string otherwise.
     *
     * @param array the array to test and possibly return
     * @return itself if it is non-null, an empty array if it is null
     */
    public static String[] nullToEmpty(String[] array) {
        return array == null ? EMPTY_STRING_ARRAY : array;
    }

}
