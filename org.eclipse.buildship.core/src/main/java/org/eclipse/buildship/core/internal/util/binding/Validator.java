/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.binding;

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
