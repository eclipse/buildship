/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * An immutable object that may contain a non-null or null reference to another object, or it may not contain any reference at all. This class is similar to the various {@code
 * Optional} implementations but differs in that it allows to store a {@code null} value. The motivation for this class is that there are scenarios where {@code null} is a valid,
 * present value and it should not be treated as an absent value. This is often the case, for example, in the Gradle Tooling API, where some APIs return {@code null} as a valid,
 * present value while under certain circumstances the same APIs do not have a value available at all. This class allows to express that distinction.
 *
 * @param <T> the type of the contained reference
 * @author Etienne Studer
 */
public final class Maybe<T> {

    private static final Maybe<Object> ABSENT = new Maybe<Object>(Optional.absent());
    private static final Object NULL_REFERENCE = new Object();

    private final Optional<T> optional;

    private Maybe(Optional<T> optional) {
        this.optional = optional;
    }

    public boolean isPresent() {
        return this.optional.isPresent();
    }

    /**
     * Returns the referenced value, if present, otherwise an exception is thrown.
     *
     * @return the referenced value, can be null
     * @throws IllegalStateException thrown if invoked on an absent instance
     */
    public T get() {
        if (this.optional.isPresent()) {
            T value = this.optional.get();
            if (value == NULL_REFERENCE) {
                return null;
            } else {
                return value;
            }
        } else {
            throw new IllegalStateException("Maybe.get() cannot be called on an absent value.");
        }
    }

    /**
     * Returns the referenced value, if present, otherwise the default value is returned.
     *
     * @param defaultValue the default value to return if no value is present
     * @return the referenced value or default value
     */
    public T or(T defaultValue) {
        return this.optional.isPresent() ? get() : defaultValue;
    }

    /**
     * Returns an {@code Maybe} instance containing the given referenced value. The referenced value can be null or non-null.
     *
     * @param reference the referenced value
     * @param <T> the type of the referenced value
     * @return the new present instance
     */
    public static <T> Maybe<T> of(T reference) {
        return new Maybe<T>(Optional.of(reference != null ? reference : Maybe.<T>typeSafeNullValue()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T typeSafeNullValue() {
        return (T) NULL_REFERENCE;
    }

    /**
     * Returns an {@code Maybe} instance with no contained reference.
     *
     * @param <T> the type if there was a referenced value
     * @return the new absent instance
     */
    public static <T> Maybe<T> absent() {
        return typeSafeAbsent();
    }

    @SuppressWarnings("unchecked")
    private static <T> Maybe<T> typeSafeAbsent() {
        return (Maybe<T>) ABSENT;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Maybe) {
            Maybe<?> other = (Maybe<?>) obj;
            return Objects.equal(this.optional, other.optional);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.optional);
    }

    @Override
    public String toString() {
        if (this.optional.isPresent()) {
            return "Maybe.of(" + this.optional.get() + ")";
        } else {
            return "Maybe.absent()";
        }
    }

}
