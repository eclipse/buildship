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

package org.eclipse.buildship.core.internal.configuration.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNatureConfiguredEvent;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNatureDeconfiguredEvent;

/**
 * Backing implementation class for the {@link org.eclipse.buildship.core.internal.configuration.GradleProjectNature}.
 * <p/>
 * Delegates to the {@link GradleProjectBuilder} to wire the project under configuration with the {@link DefaultGradleProjectBuilder}.
 * <p/>
 * Defined as an extension point of <code>org.eclipse.core.resources.natures</code> in the <i>plugin.xml</i>.
 */
public final class DefaultGradleProjectNature implements IProjectNature {

    private IProject project;

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    @Override
    public void configure() {
        GradleProjectBuilder.configureOnProject(this.project);
        CorePlugin.listenerRegistry().dispatch(new GradleProjectNatureConfiguredEvent(this.project));
    }

    @Override
    public void deconfigure() {
        GradleProjectBuilder.deconfigureOnProject(this.project);
        CorePlugin.listenerRegistry().dispatch(new GradleProjectNatureDeconfiguredEvent(this.project));
    }

}
