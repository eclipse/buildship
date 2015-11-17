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

package org.eclipse.buildship.core.workspace.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Updates the Java source settings on the target project.
 */
public final class JavaSourceSettingsUpdater {

    private static final ImmutableList<String> availableJavaVersions;

    private JavaSourceSettingsUpdater() {
    }

    static {
        // the supported Java versions vary along Eclipse releases therefore we can't query it
        // directly
        List<String> versions = new ArrayList<String>(10);
        for (Field field : JavaCore.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class) && field.getName().matches("VERSION_\\d+_\\d+")) {
                try {
                    versions.add((String) field.get(null));
                } catch (Exception e) {
                    CorePlugin.logger().error("Can't retrieve supported Java versions from JavaCore.", e);
                }
            }
        }
        availableJavaVersions = ImmutableList.copyOf(versions);
    }

    public static void update(IJavaProject project, Maybe<OmniJavaSourceSettings> sourceSettings, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Update source settings", 1);
        try {
            if (sourceSettings.isPresent() && sourceSettings.get() != null) {
                String javaSourceVersion = sourceSettings.get().getSourceLanguageLevel().getVersion().getName();
                if (isValidVersion(javaSourceVersion)) {

                    // set the source compatibility
                    boolean compilerOptionChanged = false;
                    compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_SOURCE, javaSourceVersion);
                    compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_COMPLIANCE, javaSourceVersion);

                    // TODO (donat) once the targetCompatibility is available via the Tooling API
                    // move this command to the corresponding updater class
                    compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaSourceVersion);

                    if (compilerOptionChanged) {
                        // if the compiler options have changed the project has to be rebuilt to
                        // apply the changes
                        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 1));
                    } else {
                        monitor.worked(1);
                    }
                }
            }
        } finally {
            monitor.done();
        }
    }

    private static boolean isValidVersion(String javaSourceVersion) {
        return availableJavaVersions.contains(javaSourceVersion);
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

}
