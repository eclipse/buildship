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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.configuration.GradleProjectMarker;

/**
 * Backing implementation class for the {@link org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder}.
 * <p/>
 * Wired to the project by the {@link org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder}.
 * <p/>
 * Defined as an extension point of <code>org.eclipse.core.resources.builders</code> in the <i>plugin.xml</i>.
 */
public final class DefaultGradleProjectBuilder extends IncrementalProjectBuilder {

    // In Eclipse 3.6, this method has no generics in the argument list (Map<String,String>)
    @Override
    protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        if (kind == FULL_BUILD) {
            fullBuild(project);
        } else {
            IResourceDelta delta = getDelta(project);
            if (delta == null) {
                fullBuild(project);
            } else {
                incrementalBuild(delta, project);
            }
        }
        return null;
    }

    private void fullBuild(IProject project) throws CoreException {
        // validate project
        new GradleProjectValidationResourceDeltaVisitor(project).validate();
    }

    private void incrementalBuild(IResourceDelta delta, IProject project) throws CoreException {
        // validate project
        delta.accept(new GradleProjectValidationResourceDeltaVisitor(project));
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        // delete markers
        GradleProjectMarker.INSTANCE.removeMarkerFromResourceRecursively(getProject());
    }

}
