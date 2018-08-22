/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import org.gradle.tooling.model.eclipse.EclipseOutputLocation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Updates the output location of the current project.
 *
 * @author Donat Csikos
 */
final class OutputLocationUpdater {

    public static void update(IJavaProject project, EclipseOutputLocation eclipseOutputLocation, IProgressMonitor monitor) throws CoreException {
        IPath projectPath = project.getProject().getFullPath();
        String outputPath = eclipseOutputLocation.getPath();
        project.setOutputLocation(projectPath.append(outputPath), monitor);
    }
}
