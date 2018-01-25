/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.tooling.model.eclipse.ClasspathAttribute;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;

/**
 * Default implementation of {@link OmniClasspathAttribute}.
 *
 * @author Stefan Oehme
 *
 */
final class DefaultOmniClasspathAttribute implements OmniClasspathAttribute {

    private String name;
    private String value;

    DefaultOmniClasspathAttribute(String name, String value) {
        this.name = Preconditions.checkNotNull(name);
        this.value = Preconditions.checkNotNull(value);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    static OmniClasspathAttribute from(ClasspathAttribute attribute) {
        return new DefaultOmniClasspathAttribute(attribute.getName(), attribute.getValue());
    }

}
