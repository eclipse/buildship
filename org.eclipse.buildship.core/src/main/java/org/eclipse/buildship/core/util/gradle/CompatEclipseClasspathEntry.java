/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.gradle;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.AccessRule;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Compatibility decorator for {@link EclipseClasspathEntry}.
 *
 * @param <T> the decorated type
 *
 * @author Donat Csikos
 *
 */
abstract class CompatEclipseClasspathEntry<T extends EclipseClasspathEntry> extends CompatModelElement<T> implements EclipseClasspathEntry {

    public CompatEclipseClasspathEntry(T delegate) {
        super(delegate);
    }

    @Override
    public DomainObjectSet<? extends AccessRule> getAccessRules() {
        try {
            return getElement().getAccessRules();
        } catch (Exception ignore) {
            return ModelUtils.emptyDomainObjectSet();
        }
    }

    @Override
    public DomainObjectSet<? extends ClasspathAttribute> getClasspathAttributes() {
        throw new GradlePluginsRuntimeException(ModelUtils.unsupportedMessage("getClasspathAttributes"));
    }

}
