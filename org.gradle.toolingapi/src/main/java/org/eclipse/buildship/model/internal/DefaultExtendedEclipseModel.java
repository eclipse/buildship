/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model.internal;

import java.io.Serializable;
import java.util.List;

import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseProject;

import org.eclipse.buildship.model.ProjectInGradleConfiguration;

public class DefaultExtendedEclipseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private final List<? extends ProjectInGradleConfiguration> projects;
    private final DefaultEclipseProject eclipseProject;

    public DefaultExtendedEclipseModel(List<? extends ProjectInGradleConfiguration> projects, DefaultEclipseProject eclipseProject) {
        this.projects = projects;
        this.eclipseProject = eclipseProject;
    }

    public List<? extends ProjectInGradleConfiguration> getProjects() {
        return this.projects;
    }

    public DefaultEclipseProject getEclipseProject() {
        return this.eclipseProject;
    }
}
