/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.buildship.core.util.file.RelativePathUtils;

/**
 * Updates the build script location in the persistent model.
 *
 * @author Donat Csikos
 */
final class BuildScriptLocationUpdater {

    public static void update(OmniEclipseProject eclipseProject, PersistentModelBuilder persistentModel, IProgressMonitor monitor) {
        Maybe<File> buildDirectory = eclipseProject.getGradleProject().getBuildDirectory();
        if (buildDirectory.isPresent() && buildDirectory.get() != null) {
            IPath projectPath = new Path(eclipseProject.getProjectDirectory().getAbsolutePath());
            IPath buildDirPath = new Path(buildDirectory.get().getAbsolutePath());
            persistentModel.buildScriptPath(RelativePathUtils.getRelativePath(projectPath, buildDirPath));
        } else {
            persistentModel.buildScriptPath(new Path("build.gradle"));
        }
    }
}
