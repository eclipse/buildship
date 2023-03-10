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
