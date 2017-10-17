/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.Logger;

/**
 * Classpath provider for Gradle projects which filters the project output folders based on the
 * Gradle source set information.
 *
 * @author Donat Csikos
 */
@SuppressWarnings("restriction")
public final class GradleClasspathProvider extends StandardClasspathProvider implements IRuntimeClasspathProvider {

    public static final String ID = "org.eclipse.buildship.core.classpathprovider";

    private static final String TRACE_CATEGORY = "gradleClasspathProvider";
    private static final IRuntimeClasspathEntry[] EMPTY_RESULT = new IRuntimeClasspathEntry[0];

    public GradleClasspathProvider() {
        super();
    }

    @Override
    public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
        return super.computeUnresolvedClasspath(configuration);
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

        traceResult(configuration, result);
        return result.toArray(new IRuntimeClasspathEntry[result.size()]);
    }

    private void traceResult(ILaunchConfiguration configuration, Set<IRuntimeClasspathEntry> result) {
        Logger logger = CorePlugin.logger();
        if (logger.isTraceCategoryEnabled(TRACE_CATEGORY)) {
            logger.trace(TRACE_CATEGORY, String.format("Classpath for %s: %s", configuration.getName(), result));
        }
    }

    private IRuntimeClasspathEntry[] resolveOther(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        // The project dependency entries are represented with nonstandard IRuntimeClasspathEntry
        // and resolved by DefaultEntryResolver. The code below is a copy-paste of the
        // DefaultEntryResolver except the inner resolveRuntimeClasspathEntry() method call is
        // replaced with a resolveClasspath() call. This way we can intercept the project entry
        // resolution and replace it with the resolveProject() method.
        if (entry instanceof DefaultProjectClasspathEntry) {
            List<IRuntimeClasspathEntry> result = new ArrayList<IRuntimeClasspathEntry>();
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

        Set<String> configurationScopes = GradleScopeUtils.collectScopes(configuration);
        return resolveOutputLocations(projectEntry, javaProject, configurationScopes);
    }

    private IRuntimeClasspathEntry[] resolveOptional(IRuntimeClasspathEntry entry) throws CoreException {
        if (isOptional(entry.getClasspathEntry())) {
            return EMPTY_RESULT;
        } else {
            throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
                    String.format("The project: %s which is referenced by the classpath, does not \n" + " exist", entry.getPath().lastSegment())));
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

    private static IRuntimeClasspathEntry[] resolveOutputLocations(IRuntimeClasspathEntry projectEntry, IJavaProject project, Set<String> configurationScopes)
            throws CoreException {
        List<IPath> outputLocations = Lists.newArrayList();
        boolean hasSourceFolderWithoutCustomOutput = false;

        if (project.exists() && project.getProject().isOpen()) {
            for (IClasspathEntry entry : project.getRawClasspath()) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                    // only add the output location if it's in the same source set
                    if (GradleScopeUtils.isEntryUsedByScopes(entry, configurationScopes)) {
                        IPath path = entry.getOutputLocation();
                        if (path != null) {
                            outputLocations.add(path);
                        } else {
                            // only use the default output if there's at least one source folder
                            // that doesn't have a custom output location
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
