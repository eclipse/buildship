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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Updates the Java source settings on the target project.
 */
final class JavaSourceSettingsUpdater {

    private static final ImmutableList<String> availableJavaVersions;

    static {
        // the supported Java versions vary along Eclipse releases therefore we can't query it directly
        List<String> versions = new ArrayList<String>();
        for (Field field : JavaCore.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class) && field.getName().matches("VERSION_\\d+_\\d+")) {
                try {
                    versions.add((String) field.get(null));
                } catch (Exception e) {
                    CorePlugin.logger().error("Cannot retrieve supported Java versions from JavaCore.", e);
                }
            }
        }
        Collections.sort(versions);
        availableJavaVersions = ImmutableList.copyOf(versions);
    }

    public static void update(IJavaProject project, OmniJavaSourceSettings sourceSettings, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Update Java source settings", 2);
        try {
            // obtain Java source versions
            String sourceVersion = sourceSettings.getSourceLanguageLevel().getName();
            String targetVersion = sourceSettings.getTargetBytecodeLevel().getName();

            // find or register the VM and assign it to the target project
            File vmLocation = sourceSettings.getTargetRuntime().getHomeDirectory();
            IVMInstall vm = EclipseVmUtil.findOrRegisterVM("JavaSE-" + targetVersion, vmLocation);
            addVmToClasspath(project, vm, new SubProgressMonitor(monitor, 1));

            // if the current Eclipse version doesn't support a Java version, the use the highest available
            if (!eclipseRuntimeSupportsJavaVersion(sourceVersion)) {
                sourceVersion = availableJavaVersions.get(availableJavaVersions.size() - 1);
            }
            if (!eclipseRuntimeSupportsJavaVersion(targetVersion)) {
                targetVersion = availableJavaVersions.get(availableJavaVersions.size() - 1);
            }

            // set the source levels
            boolean compilerOptionChanged = false;
            compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_COMPLIANCE, sourceVersion);
            compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_SOURCE, sourceVersion);
            compilerOptionChanged |= updateJavaProjectOptionIfNeeded(project, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, targetVersion);

            if (compilerOptionChanged) {
                // if the compiler options have changed, the project has to be rebuilt to apply the changes
                project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor, 1));
            } else {
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }

    private static void addVmToClasspath(IJavaProject project, IVMInstall vm, IProgressMonitor monitor) throws JavaModelException {
        IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
        IPath vmPath = containerPath.append(vm.getVMInstallType().getId()).append(vm.getName());
        IClasspathEntry[] classpath = project.getRawClasspath();

        // try to find the VM entry on the classpath
        for (int i = 0; i < classpath.length; i++) {
            IClasspathEntry entry = classpath[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (entry.getPath().equals(vmPath)) {
                    // if the same VM is already set then there's no need for the update
                    return;
                } else if (containerPath.isPrefixOf(entry.getPath())) {
                    // if a different VM is present then replace it
                    IClasspathEntry newContainerEntry = JavaCore.newContainerEntry(vmPath);
                    classpath[i] = newContainerEntry;
                    project.setRawClasspath(classpath, monitor);
                    return;
                }
            }
        }

        // if no VM entry is on the classpath then append it to the end
        IClasspathEntry newContainerEntry = JavaCore.newContainerEntry(vmPath);
        classpath = ObjectArrays.concat(classpath, newContainerEntry);
        project.setRawClasspath(classpath, monitor);
    }

    private static boolean eclipseRuntimeSupportsJavaVersion(String javaSourceVersion) {
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

    private JavaSourceSettingsUpdater() {
    }

}
