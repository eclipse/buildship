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

package org.eclipse.buildship.core.util.object;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class to define methods missing from Guava 15.0.
 */
public class MoreObjects {

    private MoreObjects() {
    }

    /**
     * Note: this method has been taken from
     * {@code com.google.common.base.MoreObject.firstNonNull()} method, which is available in
     * library {@code com.google.guava:18.0}.
     * <p/>
     * Returns the first of two given parameters that is not {@code null}, if either is, or
     * otherwise throws a {@link NullPointerException}.
     *
     * @return {@code first} if it is non-null; otherwise {@code second} if it is non-null
     * @throws NullPointerException if both {@code first} and {@code second} are null
     * @since 18.0 (since 3.0 as {@code Objects.firstNonNull()}.
     */
    public static <T> T firstNonNull(T first, T second) {
        return first != null ? first : checkNotNull(second);
    }

}
