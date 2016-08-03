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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Updates the source folders of the target project.
 * <p/>
 * The update is triggered via {@link #update(IJavaProject, List, IProgressMonitor)}. The method
 * executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * The update logic applies the following rules on all source folders:
 * <ul>
 * <li>If it is defined in the Gradle model and it doesn't exist in the project, then it will be
 * created.</li>
 * <li>If it was, but is no longer part of the Gradle model, then it will be deleted.</li>
 * <li>If it was created manually and is not part of the Gradle model, then it will remain
 * untouched.</li>
 * <li>If it was created manually and is also part of the Gradle model, then it will be transformed
 * such that subsequent updates will consider it coming from the Gradle model.
 * </ul>
 */
final class SourceFolderUpdater {

    private static final String CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL = "FROM_GRADLE_MODEL";

    private final IJavaProject project;
    private final List<OmniEclipseSourceDirectory> sourceFolders;

    private SourceFolderUpdater(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders) {
        this.project = Preconditions.checkNotNull(project);
        this.sourceFolders = Preconditions.checkNotNull(sourceFolders);
    }

    private void updateClasspath(IProgressMonitor monitor) throws CoreException {
        List<IClasspathEntry> gradleSourceFolders = collectGradleSourceFolders();
        List<IClasspathEntry> newClasspathEntries = calculateNewClasspath(gradleSourceFolders);
        updateClasspath(newClasspathEntries, monitor);
    }

    private ImmutableList<IClasspathEntry> collectGradleSourceFolders() throws CoreException {
        // collect all sources currently configured on the project
        Map<IPath, IClasspathEntry> sourceFolders = Maps.newHashMap();
        for (IClasspathEntry entry : this.project.getRawClasspath()) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                sourceFolders.put(entry.getPath(), entry);
            }
        }

        // collect the paths of all source folders of the new Gradle model and keep any user-defined filters
        ImmutableList.Builder<IClasspathEntry> sourceFolderEntries = ImmutableList.builder();
        for (OmniEclipseSourceDirectory sourceFolder : this.sourceFolders) {
            Optional<IClasspathEntry> entry = createSourceFolderEntry(sourceFolder, sourceFolders);
            if (entry.isPresent()) {
                sourceFolderEntries.add(entry.get());
            }
        }

        // remove duplicate source folders since JDT (IJavaProject#setRawClasspath) does not allow
        // duplicate source folders
        return ImmutableSet.copyOf(sourceFolderEntries.build()).asList();
    }

    private Optional<IClasspathEntry> createSourceFolderEntry(OmniEclipseSourceDirectory directory, Map<IPath, IClasspathEntry> sourceFolders) throws CoreException {
        // pre-condition: in case of linked resources, the linked folder must have been created already
        Optional<IFolder> linkedFolder = getLinkedFolderIfExists(directory.getDirectory());
        IResource sourceDirectory = linkedFolder.isPresent() ? linkedFolder.get() : getFolderOrProjectRoot(directory);
        if (!sourceDirectory.exists()) {
            return Optional.absent();
        }

        // preserve the previous settings by the user
        final IPackageFragmentRoot root = SourceFolderUpdater.this.project.getPackageFragmentRoot(sourceDirectory);
        SourceFolderEntryBuilder builder = new SourceFolderEntryBuilder(root.getPath());

        IClasspathEntry existingEntry = sourceFolders.get(root.getPath());
        if (existingEntry != null) {
            builder.setOutput(existingEntry.getOutputLocation());
            builder.setAttributes(existingEntry.getExtraAttributes());
            builder.setIncludes(existingEntry.getInclusionPatterns());
            builder.setExcludes(existingEntry.getExclusionPatterns());
        }

        // set source folder settings from the Gradle model
        Maybe<String> output = directory.getOutput();
        if (output.isPresent()) {
            builder.setOutput(output.get());
        }

        Optional<List<OmniClasspathAttribute>> attributes = directory.getClasspathAttributes();
        if (attributes.isPresent()) {
            builder.setAttributes(attributes.get());
        }

        Optional<List<String>> excludes = directory.getExcludes();
        if (excludes.isPresent()) {
           builder.setExcludes(excludes.get());
        }

        Optional<List<String>> includes = directory.getIncludes();
        if (includes.isPresent()) {
            builder.setIncludes(includes.get());
        }

        // mark the source folder that it is from the Gradle model
        builder.addAttribute(CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL, "true");

        return Optional.of(builder.build());
    }

    /*
     * The project root directory itself is a valid source folder.
     */
    private IResource getFolderOrProjectRoot(OmniEclipseSourceDirectory directory) {
        IProject project = this.project.getProject();
        IPath path = project.getFullPath().append(directory.getPath());
        if (path.segmentCount() == 1) {
            return project;
        } else {
            return project.getFolder(path.removeFirstSegments(1));
        }
    }

    private Optional<IFolder> getLinkedFolderIfExists(final File directory) throws CoreException {
        IResource[] children = this.project.getProject().members();
        return FluentIterable.from(Arrays.asList(children)).filter(IFolder.class).firstMatch(new Predicate<IFolder>() {

            @Override
            public boolean apply(IFolder folder) {
                return folder.isLinked() && folder.getLocation() != null && folder.getLocation().toFile().equals(directory);
            }
        });
    }

    private List<IClasspathEntry> calculateNewClasspath(List<IClasspathEntry> gradleSourceFolders) throws JavaModelException {
        // collect the paths of all source folders of the new Gradle model
        final Set<IPath> gradleModelSourcePaths = FluentIterable.from(gradleSourceFolders).transform(new Function<IClasspathEntry, IPath>() {

            @Override
            public IPath apply(IClasspathEntry entry) {
                return entry.getPath();
            }
        }).toSet();

        // collect all source folders currently configured on the project
        List<IClasspathEntry> rawClasspath = ImmutableList.copyOf(this.project.getRawClasspath());

        // filter out the source folders that are part of the new or previous Gradle model (keeping
        // only the manually added source folders)
        final List<IClasspathEntry> manuallyAddedSourceFolders = FluentIterable.from(rawClasspath).filter(new Predicate<IClasspathEntry>() {

            @Override
            public boolean apply(IClasspathEntry entry) {
                //keep everything that is not a source folder
                if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
                    return true;
                }

                //remove default entry that is auto-generated by JDT
                if (isDefaultClasspathEntry(entry)) {
                    return false;
                }

                // if a source folder is part of the new Gradle model, always treat it as a Gradle
                // source folder
                if (gradleModelSourcePaths.contains(entry.getPath())) {
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


    /*
     * JDT sets the project root as the source folder by default when converting
     * a project to a Java project. This entry prevents adding any other source folders,
     * because it overlaps with everything.
     */
    private boolean isDefaultClasspathEntry(IClasspathEntry entry) {
        return entry.getPath().equals(SourceFolderUpdater.this.project.getPath())
                && entry.getExclusionPatterns().length == 0
                && entry.getInclusionPatterns().length == 0
                && entry.getExtraAttributes().length == 0;
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
    public static void update(IJavaProject project, List<OmniEclipseSourceDirectory> sourceFolders, IProgressMonitor monitor) throws CoreException {
        SourceFolderUpdater updater = new SourceFolderUpdater(project, sourceFolders);
        updater.updateClasspath(monitor);
    }

    /**
     * Helper class to create an {@link IClasspathEntry} instance representing a source folder.
     */
    private static class SourceFolderEntryBuilder {
        private final IPath path;
        //default settings
        private String output = null;
        private List<String> includes = new ArrayList<String>();
        private List<String> excludes = new ArrayList<String>();
        private Map<String, String> attributes = new LinkedHashMap<String, String>();

        public SourceFolderEntryBuilder(IPath path) {
            this.path = path;
        }

        public void setOutput(IPath output) {
            this.output = output == null ? null : output.toPortableString();
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public void setIncludes(IPath[] includes) {
            setIncludes(pathsToStringList(includes));
        }

        public void setIncludes(List<String> includes) {
            this.includes = includes;
        }

        public void setExcludes(IPath[] excludes) {
            setExcludes(pathsToStringList(excludes));
        }

        public void setExcludes(List<String> excludes) {
            this.excludes = excludes;
        }

        public void setAttributes(IClasspathAttribute[] attributes) {
            Map<String, String> result = new LinkedHashMap<String, String>();
            for (IClasspathAttribute attr : attributes) {
                result.put(attr.getName(), attr.getValue());
            }
            this.attributes = result;
        }

        public void setAttributes(List<OmniClasspathAttribute> attributes) {
            Map<String, String> result = new LinkedHashMap<String, String>();
            for (OmniClasspathAttribute attr : attributes) {
                result.put(attr.getName(), attr.getValue());
            }
            this.attributes = result;
        }

        public void addAttribute(String name, String value) {
            this.attributes.put(name, value);
        }

        public IClasspathEntry build() {
            return JavaCore.newSourceEntry(this.path, getIncludes(), getExcludes(), getOutput(), getAttributes());
        }

        private IPath getOutput() {
            return this.output == null ? null : new Path(this.output);
        }

        private IPath[] getIncludes() {
            return stringListToPaths(this.includes);
        }

        private IPath[] getExcludes() {
            return stringListToPaths(this.excludes);
        }

        private IClasspathAttribute[] getAttributes() {
            return FluentIterable.from(this.attributes.entrySet()).transform(new Function<Entry<String, String>, IClasspathAttribute>() {

                @Override
                public IClasspathAttribute apply(Entry<String, String> entry) {
                    return JavaCore.newClasspathAttribute(entry.getKey(), entry.getValue());
                }
            }).toArray(IClasspathAttribute.class);
        }

        private static List<String> pathsToStringList(IPath[] paths) {
            List<String> result = new ArrayList<String>();
            for (IPath path : paths) {
                result.add(path.toPortableString());
            }
            return result;
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
