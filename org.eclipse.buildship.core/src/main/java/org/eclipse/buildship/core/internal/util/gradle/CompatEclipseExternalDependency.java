/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;

import org.gradle.tooling.model.ComponentSelector;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;

/**
 * Compatibility decorator for {@link EclipseExternalDependency}.
 *
 * @author Donat Csikos
 */
class CompatEclipseExternalDependency extends CompatEclipseClasspathEntry<EclipseExternalDependency> implements EclipseExternalDependency {

    public CompatEclipseExternalDependency(EclipseExternalDependency delegate) {
        super(delegate);
    }

    @Override
    public File getFile() {
        return getElement().getFile();
    }

    @Override
    public GradleModuleVersion getGradleModuleVersion() {
        return getElement().getGradleModuleVersion();
    }

    @Override
    public File getJavadoc() {
        return getElement().getJavadoc();
    }

    @Override
    public File getSource() {
        return getElement().getSource();
    }

    @Override
    public boolean isExported() {
        //  returns true for Gradle versions < 2.5
        try {
            return getElement().isExported();
        } catch (Exception ignore) {
            return true;
        }
    }

    @Override
    public boolean isResolved() {
        try {
            return getElement().isResolved();
        } catch (Exception ignore) {
            return true;
        }
    }

    @Override
    public ComponentSelector getAttemptedSelector() {
        try {
            return getElement().getAttemptedSelector();
        } catch (Exception ignore) {
            return null;
        }
    }
}
