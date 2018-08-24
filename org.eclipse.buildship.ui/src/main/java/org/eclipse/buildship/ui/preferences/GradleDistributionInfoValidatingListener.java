/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.preferences;

import java.util.Optional;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.buildship.core.GradleDistributionInfo;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Updates Gradle distribution validation messages on the preference pages.
 *
 * @author Donat Csikos
 */
final class GradleDistributionInfoValidatingListener implements DistributionChangedListener {

    private final PreferencePage preferencePage;

    public GradleDistributionInfoValidatingListener(PreferencePage preferencePage) {
        this.preferencePage = preferencePage;
    }

    @Override
    public void distributionUpdated(GradleDistributionInfo distributionInfo) {
        Optional<String> error = distributionInfo.validate();
        this.preferencePage.setValid(!error.isPresent());
        this.preferencePage.setErrorMessage(error.orElse(null));
    }
}
