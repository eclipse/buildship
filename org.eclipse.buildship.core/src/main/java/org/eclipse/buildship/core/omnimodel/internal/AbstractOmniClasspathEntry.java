/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniClasspathEntry;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.AccessRule;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import java.util.List;

/**
 * Default implementation of {@link OmniClasspathEntry}.
 *
 * @author Stefan Oehme
 *
 */
abstract class AbstractOmniClasspathEntry implements OmniClasspathEntry {

    private final Optional<List<OmniClasspathAttribute>> classpathAttributes;
    private final Optional<List<OmniAccessRule>> accessRules;

    AbstractOmniClasspathEntry(Optional<List<OmniClasspathAttribute>> classpathAttributes, Optional<List<OmniAccessRule>> accessRules) {
        this.classpathAttributes = classpathAttributes;
        this.accessRules = accessRules;
    }

    @Override
    public Optional<List<OmniClasspathAttribute>> getClasspathAttributes() {
        return this.classpathAttributes;
    }

    @Override
    public Optional<List<OmniAccessRule>> getAccessRules() {
        return this.accessRules;
    }

    protected static Optional<List<OmniClasspathAttribute>> getClasspathAttributes(EclipseClasspathEntry entry) {
        DomainObjectSet<? extends ClasspathAttribute> attributes;
        try {
            attributes = entry.getClasspathAttributes();
        } catch (UnsupportedMethodException e) {
            return Optional.absent();
        }
        Builder<OmniClasspathAttribute> builder = ImmutableList.builder();
        for (ClasspathAttribute attribute : attributes) {
            builder.add(DefaultOmniClasspathAttribute.from(attribute));
        }
        return Optional.<List<OmniClasspathAttribute>>of(builder.build());
    }

    protected static Optional<List<OmniAccessRule>> getAccessRules(EclipseClasspathEntry entry) {
        DomainObjectSet<? extends AccessRule> accessRules;
        try {
            accessRules = entry.getAccessRules();
        } catch (UnsupportedMethodException e) {
            return Optional.absent();
        }

        Builder<OmniAccessRule> builder = ImmutableList.builder();
        for (AccessRule accessRule : accessRules) {
            builder.add(DefaultOmniAccessRule.from(accessRule));
        }
        return Optional.<List<OmniAccessRule>>of(builder.build());
    }

}
