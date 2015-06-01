/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import java.io.File;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Validator that checks that the project custom location, if enabled, exists.
 */
public final class ProjectCustomLocationValidator implements IValidator {

    private final IObservableValue isDefaultLocationEnabled;

    public ProjectCustomLocationValidator(IObservableValue isDefaultLocationEnabled) {
        this.isDefaultLocationEnabled = isDefaultLocationEnabled;
    }

    @Override
    public IStatus validate(Object value) {
        if (!(Boolean) this.isDefaultLocationEnabled.getValue()) {
            if (value instanceof File) {
                File file = (File) value;
                // just check the path to the new project
                if (file.getParent() != null && !file.getParentFile().exists()) {
                    return ValidationStatus.error("The custom project location does not exist.");
                }
            }
        }
        return Status.OK_STATUS;
    }

}
