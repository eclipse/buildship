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

package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Collections;

import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings;
import org.gradle.tooling.model.eclipse.EclipseProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.internal.util.gradle.JavaVersionUtil;

/**
 * Updates the Java source settings on the target project.
 */
final class JavaSourceSettingsUpdater {

    public static void update(IJavaProject project, EclipseProject modelProject, IProgressMonitor monitor) throws CoreException {
        EclipseJavaSourceSettings sourceSettings = modelProject.getJavaSourceSettings();
        String sourceVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(sourceSettings.getSourceLanguageLevel());
        String targetVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(sourceSettings.getTargetBytecodeVersion());

        boolean compilerOptionChanged = false;
        compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_COMPLIANCE, sourceVersion);
        compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_SOURCE, sourceVersion);
        compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, targetVersion);

        if (compilerOptionChanged && isProjectAutoBuildingEnabled()) {
            scheduleJdtBuild(project.getProject());
        }
    }

    private static boolean updateJavaProjectOptionIfNeeded(IJavaProject project, String optionKey, String newValue) {
        String currentValue = project.getOption(optionKey, true);
        if (currentValue == null || !currentValue.equals(newValue)) {
            project.setOption(optionKey, newValue);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isProjectAutoBuildingEnabled() {
        return ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
    }

    private static void scheduleJdtBuild(final IProject project) {
        WorkspaceJob build = new WorkspaceJob(String.format("Building project %s after Java compiler settings changed", project.getName())) {

            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=501830
                    if (project.isAccessible()) {
                        project.build(IncrementalProjectBuilder.FULL_BUILD, JavaCore.BUILDER_ID, Collections.<String, String>emptyMap(), monitor);
                    }
                    return Status.OK_STATUS;
                } finally {
                    monitor.done();
                }
            }

            @Override
            public boolean belongsTo(Object family) {
                return family == ResourcesPlugin.FAMILY_AUTO_BUILD;
            }

        };
        build.schedule();
    }

    private JavaSourceSettingsUpdater() {
    }

}
