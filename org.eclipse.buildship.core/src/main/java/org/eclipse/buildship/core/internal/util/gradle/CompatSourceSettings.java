/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.java.InstalledJdk;

/**
 * Compatibility decorator for {@link EclipseJavaSourceSettings}.
 *
 * @author Donat Csikos
 */
class CompatSourceSettings implements EclipseJavaSourceSettings {

    private final EclipseJavaSourceSettings delegate;

    public CompatSourceSettings(EclipseJavaSourceSettings delegate) {
        this.delegate = delegate;
    }

    @Override
    public InstalledJdk getJdk() {
        // returns the JDK hosting the runtimne Eclipse for Gradle versions < 2.11
        try {
            return this.delegate.getJdk();
        } catch (Exception ignore) {
            // if the target runtime is not available, then fall back to the current JVM settings
            return CompatEclipseProject.FALLBACK_JAVA_SOURCE_SETTINGS.getJdk();
        }
    }

    @Override
    public JavaVersion getSourceLanguageLevel() {
        return this.delegate.getSourceLanguageLevel();
    }

    @Override
    public JavaVersion getTargetBytecodeVersion() {
        // returns the source language level for Gradle version < 2.11
        try {
            return this.delegate.getTargetBytecodeVersion();
        } catch (Exception ignore) {
            // if the target bytecode level is not available then
            // fall back to the current source language level
            return getSourceLanguageLevel();
        }
    }
}
