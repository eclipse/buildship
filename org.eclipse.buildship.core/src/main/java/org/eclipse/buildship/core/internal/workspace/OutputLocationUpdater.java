/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.ProjectContext;

/**
 * Updates the output location of the current project.
 *
 * @author Donat Csikos
 */
final class OutputLocationUpdater {

    public static void update(ProjectContext projectContext, IJavaProject project, EclipseProject eclipseProject, IProgressMonitor monitor) throws CoreException {
        String outputLocation = eclipseProject.getOutputLocation().getPath();
        if (sourceDirHasNestedOutputLocation(eclipseProject.getSourceDirectories(), outputLocation)) {
            String newOutputLocation = outputLocation + "-default";
            projectContext.warning("One or more source directories specify output location nested in the default output. Default output changed from " + outputLocation + " to " + newOutputLocation + ".", null);
            outputLocation = newOutputLocation;
        }
        project.setOutputLocation(project.getPath().append(outputLocation), monitor);
    }

    private static boolean sourceDirHasNestedOutputLocation(Iterable<? extends EclipseSourceDirectory> sourceDirs, String outputLocation) {
        IPath outputPath = new Path(outputLocation);
        for (EclipseSourceDirectory sourceDir : sourceDirs) {
            if (sourceDir.getOutput() != null) {
                IPath sourceDirPath = new Path(sourceDir.getOutput());
                if (!outputPath.equals(sourceDirPath) && outputPath.isPrefixOf(sourceDirPath)) {
                    return true;
                }
            }
        }
        return false;
    }
}
