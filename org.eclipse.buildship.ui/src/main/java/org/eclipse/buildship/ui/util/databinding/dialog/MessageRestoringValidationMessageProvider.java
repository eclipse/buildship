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

package org.eclipse.buildship.ui.util.databinding.dialog;

import com.google.common.base.Preconditions;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;

/**
 * Provides a given message if the validation status is OK.
 */
public final class MessageRestoringValidationMessageProvider extends ValidationMessageProvider {

    private final String message;

    public MessageRestoringValidationMessageProvider(String message) {
        this.message = Preconditions.checkNotNull(message);
    }

    @Override
    public String getMessage(ValidationStatusProvider statusProvider) {
        if (statusProvider != null) {
            IStatus status = (IStatus) statusProvider.getValidationStatus().getValue();
            return status.isOK() ? this.message : status.getMessage();
        } else {
            return this.message;
        }
    }

}
