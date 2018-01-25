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
import java.util.ListIterator;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniEclipseSourceDirectory;
import org.eclipse.buildship.core.util.gradle.Maybe;

/**
 * Updates the source folders of the target project.
 * <p/>
 * The update is triggered via {@link #update(IJavaProject, List, IProgressMonitor)}. The method
 * executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * The update logic applies the following rules on all source folders:
 * <ul>
 * <li>If it is defined in the Gradle model and it doesn't exist in the project, then it will be
 * created. Note that source folders are only created if they physically exist on disk.</li>
 * <li>If it is no longer part of the Gradle model, then it will be deleted.</li>
 * <li>The attributes, output directory and includes/excludes are only modified if present in the
 * Gradle model.</li>
 * </ul>
 */
final class SourceFolderUpdater {

    private final IJavaProject project;
    private final Map<IPath, OmniEclipseSourceDirectory> sourceFoldersByPath;

    private SourceFolderUpdater(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders) {
        this.project = Preconditions.checkNotNull(project);
        this.sourceFoldersByPath = Maps.newLinkedHashMap();
        for (OmniEclipseSourceDirectory sourceFolder : sourceFolders) {
            IPath fullPath = project.getProject().getFullPath().append(sourceFolder.getPath());
            this.sourceFoldersByPath.put(fullPath, sourceFolder);
        }
    }

    private void updateSourceFolders(IProgressMonitor monitor) throws JavaModelException {
        List<IClasspathEntry> classpath = Lists.newArrayList(this.project.getRawClasspath());
        updateExistingSourceFolders(classpath);
        addNewSourceFolders(classpath);
        this.project.setRawClasspath(classpath.toArray(new IClasspathEntry[0]), monitor);
    }

    private void updateExistingSourceFolders(List<IClasspathEntry> classpath) {
        ListIterator<IClasspathEntry> iterator = classpath.listIterator();
        while (iterator.hasNext()) {
            IClasspathEntry classpathEntry = iterator.next();
            if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IPath path = classpathEntry.getPath();
                OmniEclipseSourceDirectory sourceFolder = this.sourceFoldersByPath.get(path);
                if (sourceFolder == null) {
                    iterator.remove();
                } else {
                    iterator.set(toClasspathEntry(sourceFolder, classpathEntry));
                    this.sourceFoldersByPath.remove(path);
                }
            }
        }
    }

    private IClasspathEntry toClasspathEntry(OmniEclipseSourceDirectory sourceFolder, IClasspathEntry existingEntry) {
        SourceFolderEntryBuilder builder = new SourceFolderEntryBuilder(this.project, existingEntry.getPath());
        builder.setOutput(existingEntry.getOutputLocation());
        builder.setAttributes(existingEntry.getExtraAttributes());
        builder.setIncludes(existingEntry.getInclusionPatterns());
        builder.setExcludes(existingEntry.getExclusionPatterns());
        synchronizeAttributesFromModel(builder, sourceFolder);
        return builder.build();
    }

    private void synchronizeAttributesFromModel(SourceFolderEntryBuilder builder, OmniEclipseSourceDirectory sourceFolder) {
        Maybe<String> output = sourceFolder.getOutput();
        if (output.isPresent()) {
            builder.setOutput(output.get());
        }

        Optional<List<OmniClasspathAttribute>> attributes = sourceFolder.getClasspathAttributes();
        if (attributes.isPresent()) {
            builder.setAttributes(attributes.get());
        }

        Optional<List<String>> excludes = sourceFolder.getExcludes();
        if (excludes.isPresent()) {
            builder.setExcludes(excludes.get());
        }

        Optional<List<String>> includes = sourceFolder.getIncludes();
        if (includes.isPresent()) {
            builder.setIncludes(includes.get());
        }
    }

    private void addNewSourceFolders(List<IClasspathEntry> classpath) {
        for (OmniEclipseSourceDirectory sourceFolder : this.sourceFoldersByPath.values()) {
            IResource physicalLocation = getUnderlyingDirectory(sourceFolder);
            if (existsInSameLocation(physicalLocation, sourceFolder)) {
                classpath.add(toClasspathEntry(sourceFolder, physicalLocation));
            }
        }
    }

    private IResource getUnderlyingDirectory(OmniEclipseSourceDirectory directory) {
        IProject project = this.project.getProject();
        IPath path = project.getFullPath().append(directory.getPath());
        if (path.segmentCount() == 1) {
            return project;
        }
        return project.getFolder(path.removeFirstSegments(1));
    }

    private boolean existsInSameLocation(IResource directory, OmniEclipseSourceDirectory sourceFolder) {
        if (!directory.exists()) {
            return false;
        }
        if (directory.isLinked()) {
            return hasSameLocationAs(directory, sourceFolder);
        }
        return true;
    }

    private boolean hasSameLocationAs(IResource directory, OmniEclipseSourceDirectory sourceFolder) {
        return directory.getLocation() != null && directory.getLocation().toFile().equals(sourceFolder.getDirectory());
    }

    private IClasspathEntry toClasspathEntry(OmniEclipseSourceDirectory sourceFolder, IResource physicalLocation) {
        IPackageFragmentRoot fragmentRoot = this.project.getPackageFragmentRoot(physicalLocation);
        SourceFolderEntryBuilder builder = new SourceFolderEntryBuilder(this.project, fragmentRoot.getPath());
        synchronizeAttributesFromModel(builder, sourceFolder);
        return builder.build();
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
        SourceFolderUpdater updater = new SourceFolderUpdater(project, sourceFolders);
        updater.updateSourceFolders(monitor);
    }

    /**
     * Helper class to create an {@link IClasspathEntry} instance representing a source folder.
     */
    private static class SourceFolderEntryBuilder {

        private final IPath path;
        private IPath output = null;
        private IPath[] includes = new IPath[0];
        private IPath[] excludes = new IPath[0];
        private IClasspathAttribute[] attributes = new IClasspathAttribute[0];
        private IJavaProject project;

        public SourceFolderEntryBuilder(IJavaProject project, IPath path) {
            this.project = project;
            this.path = path;
        }

        public void setOutput(IPath output) {
            this.output = output;
        }

        public void setOutput(String output) {
            this.output = output != null ? this.project.getPath().append(output) : null;
        }

        public void setIncludes(IPath[] includes) {
            this.includes = includes;
        }

        public void setIncludes(List<String> includes) {
            this.includes = stringListToPaths(includes);
        }

        public void setExcludes(IPath[] excludes) {
            this.excludes = excludes;
        }

        public void setExcludes(List<String> excludes) {
            this.excludes = stringListToPaths(excludes);
        }

        public void setAttributes(IClasspathAttribute[] attributes) {
            this.attributes = attributes;
        }

        public void setAttributes(List<OmniClasspathAttribute> attributes) {
            this.attributes = new IClasspathAttribute[attributes.size()];
            for (int i = 0; i < attributes.size(); i++) {
                OmniClasspathAttribute attribute = attributes.get(i);
                this.attributes[i] = JavaCore.newClasspathAttribute(attribute.getName(), attribute.getValue());
            }
        }

        public IClasspathEntry build() {
            return JavaCore.newSourceEntry(this.path, this.includes, this.excludes, this.output, this.attributes);
        }

        private static IPath[] stringListToPaths(List<String> strings) {
            IPath[] result = new IPath[strings.size()];
            for (int i = 0; i < strings.size(); i++) {
                result[i] = new Path(strings.get(i));
            }
            return result;
        }
    }

}
