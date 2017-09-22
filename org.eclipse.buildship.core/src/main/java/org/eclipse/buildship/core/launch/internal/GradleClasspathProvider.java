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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

/**
 * Classpath provider for Gradle projects which filters the project output folders based on the
 * Gradle source set information.
 *
 * @author Donat Csikos
 */
@SuppressWarnings("restriction")
public final class GradleClasspathProvider extends StandardClasspathProvider implements IRuntimeClasspathProvider {

    public static final String ID = "org.eclipse.buildship.core.classpathprovider";

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
        for (int i = 0; i < entries.length; i++) {
            IRuntimeClasspathEntry entry = entries[i];
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
        // DefaultEntryResolver except the inner resolveRuntimeClasspathEntry() method call is
        // replaced with a resolveClasspath() call. This way we can intercept the project entry
        // resolution and replace it with the resolveProject() method.
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

    /*
     * From JavaRuntime.resolveRuntimeClasspathEntry()
     */
    private IRuntimeClasspathEntry[] resolveProject(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
        Set<String> mainClassSourceSets = SourceSetCollector.mainClassSourceSets(configuration);

        IResource resource = entry.getResource();
        if (resource instanceof IProject) {
            IProject p = (IProject) resource;
            IJavaProject project = JavaCore.create(p);
            if (project == null || !p.isOpen() || !project.exists()) {
                return new IRuntimeClasspathEntry[0];
            }
            IRuntimeClasspathEntry[] entries = resolveOutputLocations(project, configuration, mainClassSourceSets, entry.getClasspathProperty());
            if (entries != null) {
                return entries;
            }
        } else {
            if (isOptional(entry.getClasspathEntry())) {
                return new IRuntimeClasspathEntry[] {};
            }
        }
        return new IRuntimeClasspathEntry[] { entry };
    }

    /*
     * From JavaRuntime.resolveRuntimeClasspathEntry()
     */
    private static boolean isOptional(IClasspathEntry entry) {
        IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
        for (int i = 0, length = extraAttributes.length; i < length; i++) {
            IClasspathAttribute attribute = extraAttributes[i];
            if (IClasspathAttribute.OPTIONAL.equals(attribute.getName()) && Boolean.parseBoolean(attribute.getValue())) {
                return true;
            }
        }
        return false;
    }

    /*
     * From JavaRuntime.resolveRuntimeClasspathEntry(). The output directory is only added to the
     * classpath if the main class and the source folder are in the same source set.
     */
    private static IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project, ILaunchConfiguration configuration, Set<String> mainClassSourceSets, int classpathProperty)
            throws CoreException {
        List<IPath> nonDefault = new ArrayList<>();
        if (project.exists() && project.getProject().isOpen()) {
            IClasspathEntry entries[] = project.getRawClasspath();
            for (int i = 0; i < entries.length; i++) {
                IClasspathEntry classpathEntry = entries[i];
                if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                    if (SourceSetCollector.isEntryInSourceSets(classpathEntry, mainClassSourceSets)) {
                        IPath path = classpathEntry.getOutputLocation();
                        if (path != null) {
                            nonDefault.add(path);
                        }
                    }
                }
            }
        }
        if (nonDefault.isEmpty()) {
            return null;
        }

        IPath def = project.getOutputLocation();
        if (!nonDefault.contains(def)) {
            nonDefault.add(def);
        }
        IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
        for (int i = 0; i < locations.length; i++) {
            IClasspathEntry newEntry = JavaCore.newLibraryEntry(nonDefault.get(i), null, null);
            locations[i] = new RuntimeClasspathEntry(newEntry);
            locations[i].setClasspathProperty(classpathProperty);
        }
        return locations;
    }
}
