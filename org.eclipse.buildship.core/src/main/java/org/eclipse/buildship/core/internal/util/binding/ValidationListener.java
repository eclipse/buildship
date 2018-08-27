/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.binding;

import com.google.common.base.Optional;

/**
 * Validation listeners are invoked when a {@link Property} has been validated.
 *
 * @author Etienne Studer
 */
public interface ValidationListener {

    /**
     * Invoked when the {@link Property} instance to which this listeners is attached has been validated.
     *
     * @param source the property whose value has been validated
     * @param validationErrorMessage {@code Optional} that contains the error message iff the validation fails
     */
    void validationTriggered(Property<?> source, Optional<String> validationErrorMessage);

}
