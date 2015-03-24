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

package com.gradleware.tooling.eclipse.core.util.collections;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Contains helper methods related to Collections operations.
 */
public final class CollectionsUtils {

    private static final char SPACE = ' ';

    private CollectionsUtils() {
    }

    /**
     * Splits the given string for each space that is found.
     *
     * @param string the string to split, can be null
     * @return the split string with each segment being an element of the returned list, never null
     */
    public static ImmutableList<String> splitBySpace(String string) {
        return Strings.isNullOrEmpty(string) ? ImmutableList.<String> of() : ImmutableList.copyOf(Splitter.on(SPACE).omitEmptyStrings().splitToList(string));
    }

    /**
     * Splits the given string for each space that is found.
     *
     * @param elements the elements to join, must not be null
     * @return the joined elements returned as a single string with each element separated by a
     *         space
     */
    public static String joinWithSpace(List<String> elements) {
        return Joiner.on(SPACE).join(elements);
    }

}
