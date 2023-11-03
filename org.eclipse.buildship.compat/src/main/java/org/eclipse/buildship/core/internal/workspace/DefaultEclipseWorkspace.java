/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.gradle.tooling.model.eclipse.EclipseWorkspace;
import org.gradle.tooling.model.eclipse.EclipseWorkspaceProject;

public class DefaultEclipseWorkspace implements EclipseWorkspace, Serializable {

    private static final long serialVersionUID = 1L;
    private final File location;
    private final List<EclipseWorkspaceProject> projects;

    public DefaultEclipseWorkspace(File location, List<EclipseWorkspaceProject> projects) {
        super();
        this.location = location;
        this.projects = projects;
    }

    @Override
    public File getLocation() {
        return this.location;
    }

    @Override
    public List<EclipseWorkspaceProject> getProjects() {
        return this.projects;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.location, this.projects);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultEclipseWorkspace other = (DefaultEclipseWorkspace) obj;
        return Objects.equals(this.location, other.location) && Objects.equals(this.projects, other.projects);
    }

}
