/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.databinding.validators;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;

/**
 * This {@link IValidator} is used to check whether a {@link String} or an {@link ISelection} is
 * empty and returns a warning status in case of an empty state.
 *
 */
public class NonEmptyValidator implements IValidator {

    @Override
    public IStatus validate(Object value) {
        if (value instanceof String && ((String) value).isEmpty()) {
            return ValidationStatus.warning("This value must not be empty");
        } else if (value instanceof ISelection && ((ISelection) value).isEmpty()) {
            return ValidationStatus.warning("The selection must not be empty");
        }
        return Status.OK_STATUS;
    }

}
