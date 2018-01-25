/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.tooling.model.eclipse.EclipseProjectNature;

import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectNature;

/**
 * Default implementation of the {@link OmniEclipseProjectNature} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniEclipseProjectNature implements OmniEclipseProjectNature {

    private final String id;

    private DefaultOmniEclipseProjectNature(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public static DefaultOmniEclipseProjectNature from(EclipseProjectNature projectNature) {
        return new DefaultOmniEclipseProjectNature(projectNature.getId());
    }

}
