/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.view.task.adapter;

import java.io.File;

import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.gradle.GradleScript;

import com.google.common.base.Preconditions;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.ui.internal.view.task.ProjectNode;

/**
 * Adapts a {@link org.eclipse.buildship.ui.internal.view.task.ProjectNode} instance to a {@link IPropertySource} instance.
 */
final class ProjectNodeAdapter implements IPropertySource {

    private static final String PROPERTY_NAME = "project.name";
    private static final String PROPERTY_DESCRIPTION = "project.description";
    private static final String PROPERTY_PATH = "project.path";
    private static final String PROPERTY_TYPE = "project.type";
    private static final String PROPERTY_PROJECT_DIRECTORY = "project.directory";
    private static final String PROPERTY_BUILD_OUTPUT_DIRECTORY = "project.buildOutputDirectory";
    private static final String PROPERTY_BUILD_SCRIPT_LOCATION = "project.buildScriptLocation";

    private final GradleProject project;

    ProjectNodeAdapter(ProjectNode projectNode) {
        this.project = Preconditions.checkNotNull(projectNode).getGradleProject();
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        // @formatter:off
        return new IPropertyDescriptor[]{
                new PropertyDescriptor(PROPERTY_NAME, "Name"),
                new PropertyDescriptor(PROPERTY_DESCRIPTION, "Description"),
                new PropertyDescriptor(PROPERTY_PATH, "Path"),
                new PropertyDescriptor(PROPERTY_TYPE, "Type"),
                new PropertyDescriptor(PROPERTY_PROJECT_DIRECTORY, "Directory"),
                new PropertyDescriptor(PROPERTY_BUILD_OUTPUT_DIRECTORY, "Build Output Directory"),
                new PropertyDescriptor(PROPERTY_BUILD_SCRIPT_LOCATION, "Build Script Location"),
        };
        // @formatter:on
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME)) {
            return this.project.getName();
        } else if (id.equals(PROPERTY_DESCRIPTION)) {
            return this.project.getDescription();
        } else if (id.equals(PROPERTY_PATH)) {
            return this.project.getPath();
        } else if (id.equals(PROPERTY_TYPE)) {
            return "Gradle Project";
        } else if (id.equals(PROPERTY_PROJECT_DIRECTORY)) {
            File projectDir = this.project.getProjectDirectory();
            return FileUtils.getAbsolutePath(projectDir).or("unknown");
        } else if (id.equals(PROPERTY_BUILD_OUTPUT_DIRECTORY)) {
            File buildDir = this.project.getBuildDirectory();
            return buildDir == null ? "unknown" : buildDir;
        } else if (id.equals(PROPERTY_BUILD_SCRIPT_LOCATION)) {
            GradleScript script = this.project.getBuildScript();
            return script == null ? "unknown" : FileUtils.getAbsolutePath(script.getSourceFile()).or("none");
        } else {
            throw new IllegalStateException("Unsupported project property: " + id);
        }
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

}
