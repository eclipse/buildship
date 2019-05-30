/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import org.gradle.tooling.model.eclipse.EclipseWorkspaceProject;

class DefaultEclipseWorkspaceProject implements EclipseWorkspaceProject, Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final File location;
    private final boolean isOpen;

    public DefaultEclipseWorkspaceProject(String name, File location, boolean isOpen) {
        super();
        this.name = name;
        this.location = location;
        this.isOpen = isOpen;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public File getLocation() {
        return this.location;
    }

    @Override
    public boolean isOpen() {
        return this.isOpen;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isOpen, this.location, this.name);
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
        DefaultEclipseWorkspaceProject other = (DefaultEclipseWorkspaceProject) obj;
        return this.isOpen == other.isOpen && Objects.equals(this.location, other.location) && Objects.equals(this.name, other.name);
    }

}