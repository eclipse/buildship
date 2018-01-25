/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.io.File;

import org.gradle.api.JavaVersion;

import org.eclipse.buildship.core.omnimodel.OmniJavaRuntime;
import org.eclipse.buildship.core.omnimodel.OmniJavaVersion;

/**
 * Default implementation of the {@link OmniJavaRuntime} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaRuntime implements OmniJavaRuntime {

    private final OmniJavaVersion javaVersion;
    private final File homeDirectory;

    private DefaultOmniJavaRuntime(OmniJavaVersion javaVersion, File homeDirectory) {
        this.javaVersion = javaVersion;
        this.homeDirectory = homeDirectory;
    }

    @Override
    public OmniJavaVersion getJavaVersion() {
        return this.javaVersion;
    }

    @Override
    public File getHomeDirectory() {
        return this.homeDirectory;
    }

    public static DefaultOmniJavaRuntime from(JavaVersion javaVersion, File homeDirectory) {
        return new DefaultOmniJavaRuntime(DefaultOmniJavaVersion.from(javaVersion), homeDirectory);
    }

}
