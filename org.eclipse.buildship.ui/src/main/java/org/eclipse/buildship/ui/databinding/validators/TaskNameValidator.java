/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
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
public class TaskNameValidator implements IValidator {

    public static final String NON_ALLOWED_TASK_NAME_CHARS = ":";

    @Override
    public IStatus validate(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.isEmpty()) {
                return ValidationStatus.error("The Task Name must not be empty");
            } else if (stringValue.contains(NON_ALLOWED_TASK_NAME_CHARS)) {
                return ValidationStatus.error("The Task Name must not contain one of these characters:" + System.lineSeparator() + NON_ALLOWED_TASK_NAME_CHARS);
            }
        } else if (value instanceof ISelection && ((ISelection) value).isEmpty()) {
            return ValidationStatus.error("The selection must not be empty");
        }
        return Status.OK_STATUS;
    }

}
