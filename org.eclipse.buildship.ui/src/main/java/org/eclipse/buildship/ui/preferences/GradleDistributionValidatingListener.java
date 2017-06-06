/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.preferences;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Updates Gradle distribution validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleDistributionValidatingListener implements DistributionChangedListener {

    private final PreferencePage preferencePage;
    private final Validator<GradleDistributionWrapper> gradleDistributionValidator;

    public GradleDistributionValidatingListener(PreferencePage preferencePage, Validator<GradleDistributionWrapper> gradleDistributionValidator) {
        this.preferencePage = preferencePage;
        this.gradleDistributionValidator = gradleDistributionValidator;
    }

    @Override
    public void distributionUpdated(GradleDistributionWrapper distribution) {
        Optional<String> error = this.gradleDistributionValidator.validate(distribution);
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orNull());
    }
}
