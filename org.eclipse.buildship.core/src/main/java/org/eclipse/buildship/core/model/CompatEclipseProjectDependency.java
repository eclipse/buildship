package org.eclipse.buildship.core.model;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

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
