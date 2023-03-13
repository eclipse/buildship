/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

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
    public boolean isExported() {
        // returns true for Gradle versions < 2.5
        try {
            return getElement().isExported();
        } catch (Exception ignore) {
            return true;
        }
    }
}
