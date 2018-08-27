/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.preferences;

import com.google.common.base.Optional;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistributionInfo;
import org.eclipse.buildship.ui.internal.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Updates Gradle distribution validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleDistributionInfoValidatingListener implements DistributionChangedListener {

    private final PreferencePage preferencePage;
    private final Validator<GradleDistributionInfo> distributionInfoValidator;

    public GradleDistributionInfoValidatingListener(PreferencePage preferencePage, Validator<GradleDistributionInfo> distributionInfoValidator) {
        this.preferencePage = preferencePage;
        this.distributionInfoValidator = distributionInfoValidator;
    }

    @Override
    public void distributionUpdated(GradleDistributionInfo distributionInfo) {
        Optional<String> error = this.distributionInfoValidator.validate(distributionInfo);
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orNull());
    }
}
