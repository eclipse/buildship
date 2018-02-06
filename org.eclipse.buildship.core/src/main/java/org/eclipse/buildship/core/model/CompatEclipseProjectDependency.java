/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.model;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

/**
 * Decorated {@link EclipseProjectDependency} providing some backward compatibility.
 *
 * @author Donat Csikos
 */
public class CompatEclipseProjectDependency extends CompatEclipseClasspathEntry<EclipseProjectDependency> implements EclipseProjectDependency {

    public CompatEclipseProjectDependency(EclipseProjectDependency delegate) {
        super(delegate);
    }

    @Override
    public String getPath() {
        return this.delegate.getPath();
    }

    @Override
    @SuppressWarnings("deprecation")
    public HierarchicalEclipseProject getTargetProject() {
        return this.delegate.getTargetProject();
    }

    /**
     *  Returns true for Gradle versions < 2.5.
     */
    @Override
    public boolean isExported() {
        try {
            return this.delegate.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }
}
