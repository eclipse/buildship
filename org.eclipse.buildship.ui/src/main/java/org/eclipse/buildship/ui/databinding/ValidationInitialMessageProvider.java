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

package org.eclipse.buildship.ui.databinding;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;

/**
 * This {@link ValidationMessageProvider} restores the given initialMessage, if the validation
 * status is ok.
 *
 */
public class ValidationInitialMessageProvider extends ValidationMessageProvider {

    private String initialMessage;

    public ValidationInitialMessageProvider(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    @Override
    public String getMessage(ValidationStatusProvider statusProvider) {
        if (statusProvider != null) {
            IStatus status = (IStatus) statusProvider.getValidationStatus().getValue();
            if (status.isOK()) {
                return this.initialMessage;
            }
            return status.getMessage();
        }
        return this.initialMessage;
    }

}
