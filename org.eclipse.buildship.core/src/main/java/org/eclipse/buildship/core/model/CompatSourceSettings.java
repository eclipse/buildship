package org.eclipse.buildship.core.model;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.java.InstalledJdk;

public final class CompatSourceSettings implements EclipseJavaSourceSettings {

    private final EclipseJavaSourceSettings delegate;

    public CompatSourceSettings(EclipseJavaSourceSettings delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the JDK hosting the runtimne Eclipse if Gradle versions < 2.11.
     */
    @Override
    public InstalledJdk getJdk() {
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

    /**
     * Returns the source language level if Gradle version < 2.11.
     */
    @Override
    public JavaVersion getTargetBytecodeVersion() {
        try {
            return this.delegate.getTargetBytecodeVersion();
        } catch (Exception ignore) {
            // if the target bytecode level is not available then
            // fall back to the current source language level
            return getSourceLanguageLevel();
        }
    }
}
