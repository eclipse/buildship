/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.wizard.project;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.ValidationListener;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * A {@link ValidationListener} which decorates the target text widget with an error message when
 * the validation failed for the listened property.
 */
public final class TextDecoratingValidationListener implements ValidationListener {

    private final Text targetText;
    private final ControlDecoration decoration;

    private TextDecoratingValidationListener(Text text) {
        this.targetText = text;
        this.decoration = new ControlDecoration(this.targetText, SWT.LEFT | SWT.TOP);
    }

    public static final TextDecoratingValidationListener newInstance(Text targetText) {
        return new TextDecoratingValidationListener(targetText);
    }

    @Override
    public void validationTriggered(Property<?> property, Optional<String> message) {
        if (message.isPresent()) {
            this.decoration.setDescriptionText(message.get());
            FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
            this.decoration.setImage(fieldDecoration.getImage());
        } else {
            this.decoration.setDescriptionText("");
            this.decoration.setImage(null);
        }
    }

}
