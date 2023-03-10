/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.gradle.tooling.GradleConnector;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;

/**
 * Specifies to use the Gradle distribution defined by the target Gradle build. If the target build
 * has no Gradle distribution specified (i.e. no Gradle wrapper is used in the project) then the
 * Tooling API plug-in version will be picked.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement this interface is not intended to be implemented by clients
 */
public final class WrapperGradleDistribution extends GradleDistribution {

    WrapperGradleDistribution() {
    }

    @Override
    public void apply(GradleConnector connector) {
        connector.useBuildDistribution();
    }

    @Override
    public String toString() {
        return String.valueOf("GRADLE_DISTRIBUTION(WRAPPER)");
    }

    @Override
    public String getDisplayName() {
        return CoreMessages.GradleDistribution_Value_UseGradleWrapper;
    }
}
