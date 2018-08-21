/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

/**
 * Compatibility decorator for {@link EclipseProjectDependency}.
 *
 * @author Donat Csikos
 */
class CompatEclipseProjectDependency extends CompatEclipseClasspathEntry<EclipseProjectDependency> implements EclipseProjectDependency {

    public CompatEclipseProjectDependency(EclipseProjectDependency delegate) {
        super(delegate);
    }

    @Override
    public String getPath() {
        return getElement().getPath();
    }

    @Override
    @SuppressWarnings("deprecation")
    public HierarchicalEclipseProject getTargetProject() {
        return getElement().getTargetProject();
    }

    @Override
    public boolean isExported() {
        // returns true for Gradle versions < 2.5
        try {
            return getElement().isExported();
        } catch (Exception ignore) {
            return true;
        }
    }
}
