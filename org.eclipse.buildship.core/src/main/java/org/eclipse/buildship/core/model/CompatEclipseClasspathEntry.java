/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.eclipse.AccessRule;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import com.google.common.base.Optional;

/**
 * Decorated {@link EclipseClasspathEntry} providing some backward compatibility.
 *
 * @param <T> the decorated type
 *
 * @author Donat Csikos
 *
 */
public class CompatEclipseClasspathEntry<T extends EclipseClasspathEntry> implements EclipseClasspathEntry {

    protected final T delegate;

    public CompatEclipseClasspathEntry(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public DomainObjectSet<? extends AccessRule> getAccessRules() {
        try {
            return this.delegate.getAccessRules();
        } catch (Exception ignore) {
            return CompatHelper.emptyDomainSet();
        }
    }

    @Override
    public DomainObjectSet<? extends ClasspathAttribute> getClasspathAttributes() {

        try {
            return this.delegate.getClasspathAttributes();
        } catch (Exception ignore) {
            return CompatHelper.emptyDomainSet();
        }
    }

    public Optional<DomainObjectSet<? extends ClasspathAttribute>> getClasspathAttributesOrAbsent() {
        try {
            return Optional.<DomainObjectSet<? extends ClasspathAttribute>> of(this.delegate.getClasspathAttributes());
        } catch (Exception ignore) {
            return Optional.absent();
        }
    }

}
