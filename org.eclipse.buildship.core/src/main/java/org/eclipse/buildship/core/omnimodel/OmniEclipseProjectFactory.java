/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.buildship.core.omnimodel.internal.DefaultOmniEclipseProject;

/**
 * Factory to create {@link OmniEclipseProject} instances.
 *
 * @author Donat Csikos
 */
public class OmniEclipseProjectFactory {

    private OmniEclipseProjectFactory() {
    }

    public static OmniEclipseProject create(EclipseProject eclipseProject) {
        return DefaultOmniEclipseProject.from(eclipseProject);
    }
}
