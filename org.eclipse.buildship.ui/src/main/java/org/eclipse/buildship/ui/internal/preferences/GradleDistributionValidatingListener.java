/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.preferences;

import com.google.common.base.Optional;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.buildship.core.internal.util.binding.Validator;
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel;
import org.eclipse.buildship.ui.internal.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Updates Gradle distribution validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleDistributionValidatingListener implements DistributionChangedListener {

    private final PreferencePage preferencePage;
    private final Validator<GradleDistributionViewModel> distributionValidator;

    public GradleDistributionValidatingListener(PreferencePage preferencePage, Validator<GradleDistributionViewModel> distributionInfoValidator) {
        this.preferencePage = preferencePage;
        this.distributionValidator = distributionInfoValidator;
    }

    @Override
    public void distributionUpdated(GradleDistributionViewModel distribution) {
        Optional<String> error = this.distributionValidator.validate(distribution);
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orNull());
    }
}
