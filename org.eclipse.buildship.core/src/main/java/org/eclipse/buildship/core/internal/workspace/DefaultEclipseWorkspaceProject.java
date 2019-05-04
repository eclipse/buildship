package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

import org.gradle.tooling.model.eclipse.EclipseWorkspaceProject;

class DefaultEclipseWorkspaceProject implements EclipseWorkspaceProject, Serializable {

    private static final long serialVersionUID = 1L;
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