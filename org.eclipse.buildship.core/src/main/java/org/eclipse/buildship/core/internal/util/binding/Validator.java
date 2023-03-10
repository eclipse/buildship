/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.binding;

import com.google.common.base.Optional;

/**
 * Validates a given value and returns a validation error message if and only if the validation fails.
 *
 * @param <T> the type of the validated values
 * @author Etienne Studer
 */
public interface Validator<T> {

    /**
     * Validates the given value.
     *
     * @param value the value to validate
     * @return {@code Optional} that contains the error message iff the validation fails
     */
    Optional<String> validate(T value);

}
