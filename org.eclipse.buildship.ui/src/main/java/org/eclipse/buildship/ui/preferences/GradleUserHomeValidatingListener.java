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
import org.eclipse.buildship.ui.util.widget.GradleUserHomeGroup;

/**
 * Updates Gradle user home validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleUserHomeValidatingListener implements ModifyListener {

    private final PreferencePage preferencePage;
    private GradleUserHomeGroup gradleUserHomeGroup;
    private final Validator<File> gradleUserHomeValidator;

    public GradleUserHomeValidatingListener(PreferencePage preferencePage, GradleUserHomeGroup gradleUserHomeGroup, Validator<File> gradleUserHomeValidator) {
        this.preferencePage = preferencePage;
        this.gradleUserHomeGroup = gradleUserHomeGroup;
        this.gradleUserHomeValidator = gradleUserHomeValidator;
    }

    @Override
    public void modifyText(ModifyEvent e) {
        File gradleUserHome = this.gradleUserHomeGroup.getGradleUserHome();
        Optional<String> error = this.gradleUserHomeValidator.validate(gradleUserHome);
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orNull());
    }
}
