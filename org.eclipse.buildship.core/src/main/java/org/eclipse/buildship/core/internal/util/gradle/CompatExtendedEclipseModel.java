/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.buildship.model.ExtendedEclipseModel;
import org.eclipse.buildship.model.ProjectInGradleConfiguration;

public class CompatExtendedEclipseModel implements ExtendedEclipseModel {

    private final ExtendedEclipseModel delegate;

    public CompatExtendedEclipseModel(ExtendedEclipseModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public EclipseProject getEclipseProject() {
        return new CompatEclipseProject(this.delegate.getEclipseProject());
    }

    @Override
    public List<? extends ProjectInGradleConfiguration> getProjects() {
        return this.delegate.getProjects();
    }

}
