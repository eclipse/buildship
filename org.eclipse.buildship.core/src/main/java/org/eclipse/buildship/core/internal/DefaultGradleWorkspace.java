/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;

/**
 * Default implementation for {@link GradleWorkspace}.
 *
 * @author Donat Csikos
 */
public final class DefaultGradleWorkspace implements GradleWorkspace {

    @Override
    public GradleBuild getBuild(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(GradleProjectNature.isPresentOn(project));
        return DefaultGradleBuild.from(project);
    }
}
