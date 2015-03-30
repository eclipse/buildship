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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.gradleware.tooling.eclipse.core.project.GradleProjectMarkers;

/**
 * Implementation class for the {@link GradleProjectBuilders#DEFAULT_BUILDER} builder definition.
 */
public final class DefaultGradleProjectBuilder extends IncrementalProjectBuilder {

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        // delete markers
        GradleProjectMarkers.MISSING_CONFIGURATION_MARKER.removeMarkerFromResourceRecursively(getProject());
    }

    @Override
    // In Eclipse 3.6 this method has no generics in the argument list
    protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map/* <String,String> */args, IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        if (kind == FULL_BUILD) {
            fullBuild(project, monitor);
        } else {
            IResourceDelta delta = getDelta(project);
            if (delta == null) {
                fullBuild(project, monitor);
            } else {
                incrementalBuild(delta, project, monitor);
            }
        }
        return null;
    }

    private void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException {
        new ValidationTriggeringResourceDeltaVisitor(project).validate();
    }

    private void incrementalBuild(IResourceDelta delta, IProject project, IProgressMonitor monitor) throws CoreException {
        delta.accept(new ValidationTriggeringResourceDeltaVisitor(project));
    }
}
