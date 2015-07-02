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

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * TODO (donat) add documentation.
 */
public final class SourceFolderUpdater {

    private static final String CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL = "FROM_GRADLE_MODEL";

    private IJavaProject project;
    private List<OmniEclipseSourceDirectory> sourceFolders;

    public SourceFolderUpdater(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders) {
        this.project = Preconditions.checkNotNull(project);
        this.sourceFolders = Preconditions.checkNotNull(sourceFolders);
    }

    public void updateClasspath() throws JavaModelException {
        List<IClasspathEntry> gradleSourceFolders = collectGradleSourceFolders();
        List<IClasspathEntry> newClasspathEntries = calculateNewClasspath(gradleSourceFolders);
        updateClasspath(newClasspathEntries);
    }

    private List<IClasspathEntry> collectGradleSourceFolders() {
        List<IClasspathEntry> sourceFolders = FluentIterable.from(this.sourceFolders).transform(new Function<OmniEclipseSourceDirectory, IClasspathEntry>() {

            @Override
            public IClasspathEntry apply(OmniEclipseSourceDirectory directory) {
                IFolder sourceDirectory = SourceFolderUpdater.this.project.getProject().getFolder(Path.fromOSString(directory.getPath()));
                FileUtils.ensureFolderHierarchyExists(sourceDirectory);
                IPackageFragmentRoot root = SourceFolderUpdater.this.project.getPackageFragmentRoot(sourceDirectory);
                IClasspathAttribute fromGradleModel = JavaCore.newClasspathAttribute(CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL, "true");
                // @formatter:off
                return JavaCore.newSourceEntry(root.getPath(),
                        new IPath[]{},                               // include all files
                        new IPath[]{},                               // don't exclude anything
                        null,                                        // use the same output folder as defined on the project
                        new IClasspathAttribute[]{fromGradleModel}   // the source folder is loaded from the current Gradle model
                );
                // @formatter:on
            }
        }).toList();

        // remove duplicate source folders since JDT (IJavaProject#setRawClasspath) does not allow
        // duplicate source folders
        return ImmutableSet.copyOf(sourceFolders).asList();
    }

    private List<IClasspathEntry> calculateNewClasspath(List<IClasspathEntry> gradleSourceFolders) throws JavaModelException {
        // collect the paths of all source folders of the new Gradle model
        final Set<String> gradleModelSourcePaths = FluentIterable.from(gradleSourceFolders).transform(new Function<IClasspathEntry, String>() {

            @Override
            public String apply(IClasspathEntry entry) {
                return entry.getPath().toString();
            }
        }).toSet();

        // collect all source folders currently configured on the project
        List<IClasspathEntry> rawClasspath = ImmutableList.copyOf(this.project.getRawClasspath());

        // filter out the source folders that are part of the new or previous Gradle model (keeping
        // only the manually added source folders)
        final List<IClasspathEntry> manuallyAddedSourceFolders = FluentIterable.from(rawClasspath).filter(new Predicate<IClasspathEntry>() {

            @Override
            public boolean apply(IClasspathEntry entry) {
                // if a source folder is part of the new Gradle model, always treat it as a Gradle
                // source folder
                if (gradleModelSourcePaths.contains(entry.getPath().toString())) {
                    return false;
                }

                // if a source folder is marked as coming from a previous Gradle model, treat it as
                // a Gradle source folder
                for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
                    if (attribute.getName().equals(CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL) && attribute.getValue().equals("true")) {
                        return false;
                    }
                }

                // treat it as a manually added source folder
                return true;
            }
        }).toList();

        // new classpath = current source folders from the Gradle model + the previous ones defined
        // manually
        return ImmutableList.<IClasspathEntry> builder().addAll(gradleSourceFolders).addAll(manuallyAddedSourceFolders)
                .build();

    }

    private void updateClasspath(List<IClasspathEntry> newClasspathEntries) throws JavaModelException {
        IClasspathEntry[] newRawClasspath = newClasspathEntries.toArray(new IClasspathEntry[newClasspathEntries.size()]);
        this.project.setRawClasspath(newRawClasspath, new NullProgressMonitor());
    }
}
