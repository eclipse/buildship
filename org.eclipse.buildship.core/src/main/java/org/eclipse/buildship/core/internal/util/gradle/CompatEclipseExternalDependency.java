/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;

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
}
