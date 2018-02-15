/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import java.util.Collections;
import java.util.List;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

import com.google.common.base.Optional;

/**
 * Contains helper methods related to the Tooling API models.
 *
 * @author Donat Csikos
 */
public final class ModelUtils {

    private ModelUtils() {
    }

    /**
     * Creates decorator for the target {@link EclipseProject} that returns sensible defaults for
     * attributes for older Gradle versions.
     * <p>
     * There are a few use-cases where Buildship needs to distinguish if the model element is
     * missing from the model or not. In those cases, the attributes need to be accessed via the
     * helper methods in this class.
     *
     * @param model the target model
     * @return the decorated model
     */
    public static EclipseProject createCompatibilityModel(EclipseProject model) {
        return new CompatEclipseProject(model);
    }

    public static Maybe<String> getOutput(EclipseSourceDirectory sourceDirectory) {
        if (sourceDirectory instanceof CompatEclipseSourceDirectory) {
        }
        try {
            return Maybe.of(extractRawModel(sourceDirectory).getOutput());
        } catch (Exception ignore) {
            return Maybe.absent();
        }
    }

    public static Optional<List<String>> getExcludes(EclipseSourceDirectory sourceDirectory) {
        try {
            return Optional.of(extractRawModel(sourceDirectory).getExcludes());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    public static Optional<List<String>> getIncludes(EclipseSourceDirectory sourceDirectory) {
        try {
            return Optional.of(extractRawModel(sourceDirectory).getIncludes());
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    public static Optional<Iterable<? extends ClasspathAttribute>> getClasspathAttributes(EclipseClasspathEntry classpathEntry) {
        try {
            return Optional.<Iterable<? extends ClasspathAttribute>> of(extractRawModel(classpathEntry).getClasspathAttributes());
        } catch (Exception ignore) {
            return Optional.absent();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T extractRawModel(T model) {
        if (model instanceof CompatModelElement<?>) {
            return ((CompatModelElement<T>) model).getElement();
        }

        return model;
    }

    static <T> DomainObjectSet<? extends T> asDomainObjectSet(Iterable<? extends T> result) {
        return ImmutableDomainObjectSet.of(result);
    }

    static <T> DomainObjectSet<? extends T> emptyDomainObjectSet() {
        return ImmutableDomainObjectSet.of(Collections.<T> emptyList());
    }

    static String unsupportedMessage(String methodName) {
        return "Should have called " + ModelUtils.class.getSimpleName() + "." + methodName + "()";
    }
}
