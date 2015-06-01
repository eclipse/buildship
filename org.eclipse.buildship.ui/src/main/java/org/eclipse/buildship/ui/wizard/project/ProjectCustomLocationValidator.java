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

import com.google.common.base.Optional;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.io.File;

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
            File file = (File) value;
            if (file != null) {
                file = file.getParentFile();
            }
            Optional<String> validation = Validators.requiredDirectoryValidator("Custom project location").validate(file);
            if (validation.isPresent()) {
                return ValidationStatus.error(validation.get());
            }
        }
        return Status.OK_STATUS;
    }

}
