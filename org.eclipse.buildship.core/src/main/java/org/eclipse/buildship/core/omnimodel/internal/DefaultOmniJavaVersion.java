/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.api.JavaVersion;

import org.eclipse.buildship.core.omnimodel.OmniJavaVersion;

/**
 * Default implementation of the {@link OmniJavaVersion} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaVersion implements OmniJavaVersion {

    private final String name;

    private DefaultOmniJavaVersion(JavaVersion javaVersion) {
        this.name = javaVersion.isJava9Compatible() ? javaVersion.getMajorVersion() : javaVersion.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static DefaultOmniJavaVersion from(JavaVersion name) {
        return new DefaultOmniJavaVersion(name);
    }

}
