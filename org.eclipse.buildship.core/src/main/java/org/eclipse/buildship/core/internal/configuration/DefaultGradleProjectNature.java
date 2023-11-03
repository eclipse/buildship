/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

import org.eclipse.buildship.core.internal.CorePlugin;

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
