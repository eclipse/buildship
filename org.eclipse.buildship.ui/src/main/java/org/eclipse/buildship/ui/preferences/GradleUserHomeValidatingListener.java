/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.eclipse.buildship.ui.preferences;

import java.io.File;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.launch.LaunchMessages;

/**
 * Updates Gradle user home validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleUserHomeValidatingListener implements ModifyListener {

    private final PreferencePage preferencePage;
    private final Validator<File> gradleUserHomeValidator;

    public GradleUserHomeValidatingListener(PreferencePage preferencePage, Validator<File> gradleUserHomeValidator) {
        this.preferencePage = preferencePage;
        this.gradleUserHomeValidator = gradleUserHomeValidator;
    }

    @Override
    public void modifyText(ModifyEvent e) {
        String resolvedGradleUserHome = getResolvedGradleUserHome(((Text)e.widget).getText());
        File gradleUserHome = FileUtils.getAbsoluteFile(resolvedGradleUserHome).orNull();
        Optional<String> error = this.gradleUserHomeValidator.validate(gradleUserHome);
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orNull());
    }

    private String getResolvedGradleUserHome(String gradleUserHomeExpression) {
        // TODO (donat) (BUG) I don't think we decode expressions in the workspace / project preferences
        gradleUserHomeExpression = Strings.emptyToNull(gradleUserHomeExpression);

        String gradleUserHomeResolved = null;
        try {
            gradleUserHomeResolved = ExpressionUtils.decode(gradleUserHomeExpression);
        } catch (CoreException e) {
            this.preferencePage.setErrorMessage(NLS.bind(LaunchMessages.ErrorMessage_CannotResolveExpression_0, gradleUserHomeExpression));
            this.preferencePage.setValid(false);
        }
        return gradleUserHomeResolved;
    }
}
