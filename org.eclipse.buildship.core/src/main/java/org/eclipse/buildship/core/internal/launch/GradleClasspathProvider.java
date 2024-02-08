/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Classpath provider for Gradle projects filtering the project output folders based on the Gradle
 * dependency scope information.
 *
 * @author Donat Csikos
 */
@SuppressWarnings("restriction")
public final class GradleClasspathProvider extends StandardClasspathProvider implements IRuntimeClasspathProvider {

    public static final String ID = "org.eclipse.buildship.core.classpathprovider";

    private static final IRuntimeClasspathEntry[] EMPTY_RESULT = new IRuntimeClasspathEntry[0];

    public GradleClasspathProvider() {
        super();
    }

    @Override
    public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
        return filterUnusedDependencies(configuration, super.computeUnresolvedClasspath(configuration));
    }

    private IRuntimeClasspathEntry[] filterUnusedDependencies(ILaunchConfiguration configuration, IRuntimeClasspathEntry[] entriesToFilter) throws CoreException {
        // if the run configuration uses Java 9 then the library dependencies are already present in the
        // unresolved classpath. That is because the Java 9 support calculates the class/module path from
        // the result of IJavaProject.getResolvedClasspath(true). Unfortunately, the runtime entries don't
        // have the source set attribute, so we have to filter them base on entry paths.
        IJavaProject project = JavaRuntime.getJavaProject(configuration);
        IClasspathEntry[] classpath = project.getResolvedClasspath(true);
        LaunchConfigurationScope configurationScopes = LaunchConfigurationScope.from(configuration);
        Set<IPath> excludedPaths = Sets.newHashSet();
        for (IClasspathEntry entry : classpath) {
            if (!configurationScopes.isEntryIncluded(entry)) {
                excludedPaths.add(entry.getPath());
            }
        }

        List<IRuntimeClasspathEntry> result = new ArrayList<>(entriesToFilter.length);
        for (IRuntimeClasspathEntry  entry : entriesToFilter) {
            if (!excludedPaths.contains(entry.getPath())) {
                result.add(entry);
            }
        }

        return result.toArray(new IRuntimeClasspathEntry[0]);
    }

    @Override
    public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
        Set<IRuntimeClasspathEntry> result = new LinkedHashSet<>(entries.length);
        for (IRuntimeClasspathEntry entry : entries) {
            switch (entry.getType()) {
                case IRuntimeClasspathEntry.OTHER:
                    Collections.addAll(result, resolveOther(entry, configuration));
                    break;
                case IRuntimeClasspathEntry.PROJECT:
                    Collections.addAll(result, resolveProject(entry, configuration));
                    break;
                default:
                    Collections.addAll(result, JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration));
                    break;
            }
        }

        return result.toArray(new IRuntimeClasspathEntry[result.size()]);
    }

    private IRuntimeClasspathEntry[] resolveOther(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        // The project dependency entries are represented with nonstandard IRuntimeClasspathEntry
        // and resolved by DefaultEntryResolver. The code below is a copy-paste of the
        // DefaultEntryResolver except that the inner resolveRuntimeClasspathEntry() method call is
        // replaced with a resolveClasspath(). This way we can intercept and update the project
        // entry resolution using the resolveProject() method.
        if (entry instanceof DefaultProjectClasspathEntry) {
            List<IRuntimeClasspathEntry> result = new ArrayList<>();
            for (IRuntimeClasspathEntry e : ((IRuntimeClasspathEntry2) entry).getRuntimeClasspathEntries(configuration)) {
                Collections.addAll(result, resolveClasspath(new IRuntimeClasspathEntry[] { e }, configuration));
            }
            return result.toArray(new IRuntimeClasspathEntry[result.size()]);
        } else {
            return JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration);
        }
    }

    private IRuntimeClasspathEntry[] resolveProject(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        IResource resource = entry.getResource();
        if (resource instanceof IProject) {
            return resolveProject(entry, (IProject) resource, configuration);
        } else {
            return resolveOptional(entry);
        }
    }

    private IRuntimeClasspathEntry[] resolveProject(IRuntimeClasspathEntry projectEntry, IProject project, ILaunchConfiguration configuration) throws CoreException {
        if (!project.isOpen()) {
            return EMPTY_RESULT;
        }

        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject == null || !javaProject.exists()) {
            return EMPTY_RESULT;
        }

        LaunchConfigurationScope configurationScopes = LaunchConfigurationScope.from(configuration);
        return resolveOutputLocations(projectEntry, javaProject, configurationScopes);
    }

    private IRuntimeClasspathEntry[] resolveOptional(IRuntimeClasspathEntry entry) throws CoreException {
        if (isOptional(entry.getClasspathEntry())) {
            return EMPTY_RESULT;
        } else {
            throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
                    String.format("The project: %s which is referenced by the classpath, does not exist", entry.getPath().lastSegment())));
        }
    }

    private static boolean isOptional(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (IClasspathAttribute.OPTIONAL.equals(attribute.getName()) && Boolean.parseBoolean(attribute.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static IRuntimeClasspathEntry[] resolveOutputLocations(IRuntimeClasspathEntry projectEntry, IJavaProject project, LaunchConfigurationScope configurationScopes)
            throws CoreException {
        List<IPath> outputLocations = Lists.newArrayList();
        boolean hasSourceFolderWithoutCustomOutput = false;

        if (project.exists() && project.getProject().isOpen()) {
            for (IClasspathEntry entry : project.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                    // only add the output location if it's in the same source set
                    if (configurationScopes.isEntryIncluded(entry)) {
                        IPath path = entry.getOutputLocation();
                        if (path != null) {
                            outputLocations.add(path);
                        } else {
                            // only use the default output if there's at least one source folder that doesn't have a custom output location
                            hasSourceFolderWithoutCustomOutput = true;
                        }
                    }
                }
            }
        }

        if (outputLocations.isEmpty()) {
            return new IRuntimeClasspathEntry[] { projectEntry };
        }

        IPath defaultOutputLocation = project.getOutputLocation();
        if (!outputLocations.contains(defaultOutputLocation) && hasSourceFolderWithoutCustomOutput) {
            outputLocations.add(defaultOutputLocation);
        }

        IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[outputLocations.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = new RuntimeClasspathEntry(JavaCore.newLibraryEntry(outputLocations.get(i), null, null));
            result[i].setClasspathProperty(projectEntry.getClasspathProperty());
        }
        return result;
    }
}
