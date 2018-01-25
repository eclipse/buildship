/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.tooling.model.eclipse.EclipseOutputLocation;

import org.eclipse.buildship.core.omnimodel.OmniEclipseOutputLocation;

/**
 * Default implementation of {@link OmniEclipseOutputLocation}.
 *
 * @author Donat Csikos
 */
final class DefaultOmniEclipseOutputLocation implements OmniEclipseOutputLocation {

    private final String path;

    DefaultOmniEclipseOutputLocation(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseOutputLocation from(EclipseOutputLocation outputLocation) {
        return new DefaultOmniEclipseOutputLocation(outputLocation.getPath());
    }
}

