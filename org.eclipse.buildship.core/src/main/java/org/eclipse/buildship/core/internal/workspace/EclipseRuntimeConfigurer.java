/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.tooling.model.eclipse.EclipseRuntime;
import org.gradle.tooling.model.eclipse.EclipseWorkspace;
import org.gradle.tooling.model.eclipse.EclipseWorkspaceProject;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.buildship.core.internal.CorePlugin;

public class EclipseRuntimeConfigurer implements Action<EclipseRuntime>, Serializable {

    private final EclipseWorkspace workspace;

    public EclipseRuntimeConfigurer() {
        ImmutableList<IProject> allWorkspaceProjects = CorePlugin.workspaceOperations().getAllProjects();
        List<EclipseWorkspaceProject> projects = allWorkspaceProjects.stream().map(p -> new DefaultEclipseWorkspaceProject(p.getName(), p.getLocation().toFile()))
                .collect(Collectors.toList());
        this.workspace = new DefaultEclipseWorkspace(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), projects);
    }

    @Override
    public void execute(EclipseRuntime t) {
        t.setWorkspace(this.workspace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.workspace);
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
        EclipseRuntimeConfigurer other = (EclipseRuntimeConfigurer) obj;
        return Objects.equals(this.workspace, other.workspace);
    }

    private static class DefaultEclipseWorkspace implements EclipseWorkspace, Serializable {

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

    private static class DefaultEclipseWorkspaceProject implements EclipseWorkspaceProject, Serializable {

        private final String name;
        private final File location;

        public DefaultEclipseWorkspaceProject(String name, File location) {
            super();
            this.name = name;
            this.location = location;
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
        public int hashCode() {
            return Objects.hash(this.location, this.name);
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
            return Objects.equals(this.location, other.location) && Objects.equals(this.name, other.name);
        }

    }
}
