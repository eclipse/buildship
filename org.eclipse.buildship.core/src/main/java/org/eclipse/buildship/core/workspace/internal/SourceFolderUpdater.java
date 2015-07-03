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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * Updates the source folders of the target project.
 * <p/>
 * To execute the update call the static {@link #update(IJavaProject, List, IProgressMonitor)} method. The method
 * executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * The update logic applies the following rules on all source folders:
 * <ul>
 * <li>If it is defined in the Gradle model and it doesn't exist in the project, then it will be
 * created.</li>
 * <li>If it was, but is no longer part of the model, then it will be deleted.</li>
 * <li>If it was created manually and is not part of the Gradle model, then it will remain
 * untouched.</li>
 * <li>If it was created manually and is also part of the Gradle model, then it will be transformed
 * such that subsequent updates will consider it coming from the model.
 * </ul>
 */
public final class SourceFolderUpdater {

    private static final String CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL = "FROM_GRADLE_MODEL";

    private final IJavaProject project;
    private final List<OmniEclipseSourceDirectory> sourceFolders;

    private SourceFolderUpdater(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders) {
        this.project = Preconditions.checkNotNull(project);
        this.sourceFolders = Preconditions.checkNotNull(sourceFolders);
    }

    private void updateClasspath(IProgressMonitor monitor) throws JavaModelException {
        List<IClasspathEntry> gradleSourceFolders = collectGradleSourceFolders();
        List<IClasspathEntry> newClasspathEntries = calculateNewClasspath(gradleSourceFolders);
        updateClasspath(newClasspathEntries, monitor);
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
        return ImmutableList.<IClasspathEntry> builder().addAll(gradleSourceFolders).addAll(manuallyAddedSourceFolders).build();
    }

    private void updateClasspath(List<IClasspathEntry> newClasspathEntries, IProgressMonitor monitor) throws JavaModelException {
        IClasspathEntry[] newRawClasspath = newClasspathEntries.toArray(new IClasspathEntry[newClasspathEntries.size()]);
        this.project.setRawClasspath(newRawClasspath, monitor);
    }

    /**
     * Updates the source folders on the target project.
     *
     * @param project the target project to update the source folders on
     * @param sourceFolders the list of source folders from the Gradle model to assign to the
     *            project
     * @param monitor the monitor to report progress on
     * @throws JavaModelException if the classpath modification fails
     */
    public static void update(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders, IProgressMonitor monitor) throws JavaModelException {
        new SourceFolderUpdater(project, sourceFolders).updateClasspath(monitor);
    }

}
