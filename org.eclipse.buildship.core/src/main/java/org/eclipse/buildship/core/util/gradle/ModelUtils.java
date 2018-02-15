/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import java.util.Collections;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

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
     * helper methods defined in the Compatibility model classes.
     *
     * @param model the target model
     * @return the decorated model
     */
    public static EclipseProject createCompatibilityModel(EclipseProject model) {
        return new CompatEclipseProject(model);
    }

    static <T> DomainObjectSet<? extends T> asDomainObjectSet(Iterable<? extends T> result) {
        return ImmutableDomainObjectSet.of(result);
    }

    static <T> DomainObjectSet<? extends T> emptyDomainObjectSet() {
        return ImmutableDomainObjectSet.of(Collections.<T> emptyList());
    }
}
