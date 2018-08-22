/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.File;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleScript;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.internal.util.file.RelativePathUtils;

/**
 * Updates the build script location in the persistent model.
 *
 * @author Donat Csikos
 */
final class BuildScriptLocationUpdater {

    public static void update(EclipseProject eclipseProject, PersistentModelBuilder persistentModel, IProgressMonitor monitor) {
        GradleScript buildScript = eclipseProject.getGradleProject().getBuildScript();
        if (buildScript != null) {
            IPath projectPath = new Path(eclipseProject.getProjectDirectory().getAbsolutePath());
            IPath buildScriptPath;
            File sourceFile = buildScript.getSourceFile();
            if (sourceFile != null) {
                buildScriptPath = new Path(sourceFile.getAbsolutePath());
            } else {
                buildScriptPath = new Path(new File("build.gradle").getAbsolutePath());
            }
            persistentModel.buildScriptPath(RelativePathUtils.getRelativePath(projectPath, buildScriptPath));
        } else {
            persistentModel.buildScriptPath(new Path("build.gradle"));
        }
    }
}
