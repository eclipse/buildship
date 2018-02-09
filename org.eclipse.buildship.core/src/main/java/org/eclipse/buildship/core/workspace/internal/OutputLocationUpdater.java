/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.omnimodel.OmniEclipseOutputLocation;

/**
 * Updates the output location of the current project.
 *
 * @author Donat Csikos
 */
final class OutputLocationUpdater {

    public static void update(IJavaProject project, Optional<OmniEclipseOutputLocation> outputLocation, IProgressMonitor monitor) throws CoreException {
        if (outputLocation.isPresent()) {
            IPath projectPath = project.getProject().getFullPath();
            String outputPath = outputLocation.get().getPath();
            project.setOutputLocation(projectPath.append(outputPath), monitor);
        }
    }
}
