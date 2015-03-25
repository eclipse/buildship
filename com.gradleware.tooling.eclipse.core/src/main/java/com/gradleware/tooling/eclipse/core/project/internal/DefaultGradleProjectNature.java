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

package com.gradleware.tooling.eclipse.core.project.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

import com.gradleware.tooling.eclipse.core.project.GradleProjectBuilders;
import com.gradleware.tooling.eclipse.core.project.GradleProjectNatures;

/**
 * Implementation class for the {@link GradleProjectNatures#DEFAULT_NATURE} nature definition.
 * <p/>
 * It automatically registers the {@link GradleProjectBuilders#DEFAULT_BUILDER} build on the project
 * when it is associated with a project.
 */
public final class DefaultGradleProjectNature implements IProjectNature {

    private IProject project;

    @Override
    public void configure() {
        GradleProjectBuilders.DEFAULT_BUILDER.configureOnProject(this.project);
    }

    @Override
    public void deconfigure() {
        GradleProjectBuilders.DEFAULT_BUILDER.deconfigureOnProject(this.project);
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }
}
