/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import java.io.File;

import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;

/**
 * Decorated {@link EclipseExternalDependency} providing some backward compatibility.
 *
 * @author Donat Csikos
 */
public class CompatEclipseExternalDependency extends CompatEclipseClasspathEntry<EclipseExternalDependency> implements EclipseExternalDependency {

    public CompatEclipseExternalDependency(EclipseExternalDependency delegate) {
        super(delegate);
    }

    @Override
    public File getFile() {
        return this.delegate.getFile();
    }

    @Override
    public GradleModuleVersion getGradleModuleVersion() {
        return this.delegate.getGradleModuleVersion();
    }

    @Override
    public File getJavadoc() {
        return this.delegate.getJavadoc();
    }

    @Override
    public File getSource() {
        return this.delegate.getSource();
    }

    /**
     *  Returns true for Gradle versions < 2.5.
     */
    @Override
    public boolean isExported() {
        try {
            return this.delegate.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }
}
