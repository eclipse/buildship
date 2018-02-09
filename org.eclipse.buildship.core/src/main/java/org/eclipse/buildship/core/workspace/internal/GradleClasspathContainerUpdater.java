/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473348
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectDependency;
import org.eclipse.buildship.core.omnimodel.OmniExternalDependency;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.util.classpath.ClasspathUtils;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates the classpath container of the target project.
 * <p/>
 * The update is triggered via {@link #updateFromModel(IJavaProject, OmniEclipseProject, Set, IProgressMonitor)}.
 * The method executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * The update logic composes a new classpath container containing all project and external
 * dependencies defined in the Gradle model. At the end of the execution the old classpath
 * container is replaced by the one being created.
 * <p/>
 * If an invalid external dependency is received (anything else, than a folder, {@code .jar} file
 * or {@code .zip} file) the given entry is omitted from the classpath container. Due to
 * performance reasons only the file extension is checked.
 */
final class GradleClasspathContainerUpdater {

    private final IJavaProject eclipseProject;
    private final OmniEclipseProject gradleProject;
    private final Map<File, OmniEclipseProject> projectDirToProject;

    private GradleClasspathContainerUpdater(IJavaProject eclipseProject, OmniEclipseProject gradleProject, Set<OmniEclipseProject> allGradleProjects) {
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
        this.projectDirToProject = Maps.newHashMap();
        for (OmniEclipseProject project : gradleProject.getRoot().getAll()) {
            this.projectDirToProject.put(project.getProjectDirectory(), project);
        }
    }

    private void updateClasspathContainer(PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws JavaModelException {
        ImmutableList<IClasspathEntry> containerEntries = collectClasspathContainerEntries();
        setClasspathContainer(this.eclipseProject, containerEntries, monitor);
        persistentModel.classpath(containerEntries);
    }

    private ImmutableList<IClasspathEntry> collectClasspathContainerEntries() {
        List<IClasspathEntry> externalDependencies = collectExternalDependencies();
        List<IClasspathEntry> projectDependencies = collectProjectDependencies();

        boolean hasExportedEntry = FluentIterable.from(externalDependencies).anyMatch(new Predicate<IClasspathEntry>() {

            @Override
            public boolean apply(IClasspathEntry entry) {
                return entry.isExported();
            }
        });

        // Gradle distributions <2.5 rely on exports to define the project classpath. Unfortunately
        // that logic is broken if dependency excludes are defined in the build scripts. To work
        // around that, external dependencies must be defined before project dependencies. For more
        // details, visit Bug 473348.
        if (hasExportedEntry) {
            return ImmutableList.<IClasspathEntry>builder().addAll(externalDependencies).addAll(projectDependencies).build();
        } else {
            return ImmutableList.<IClasspathEntry>builder().addAll(projectDependencies).addAll(externalDependencies).build();
        }
    }

    private List<IClasspathEntry> collectExternalDependencies() {
        Builder<IClasspathEntry> result = ImmutableList.builder();
        for (OmniExternalDependency dependency : this.gradleProject.getExternalDependencies()) {
            File dependencyFile = dependency.getFile();
            boolean linkedResourceCreated = tryCreatingLinkedResource(dependencyFile, result);
            if (!linkedResourceCreated) {
                String dependencyName = dependencyFile.getName();
                // Eclipse only accepts folders and archives as external dependencies (but not, for example, a DLL)
                if (dependencyFile.isDirectory() || dependencyName.endsWith(".jar") || dependencyName.endsWith(".zip")) {
                    IPath path = org.eclipse.core.runtime.Path.fromOSString(dependencyFile.getAbsolutePath());
                    File dependencySource = dependency.getSource();
                    IPath sourcePath = dependencySource != null ? org.eclipse.core.runtime.Path.fromOSString(dependencySource.getAbsolutePath()) : null;
                    IClasspathEntry entry = JavaCore.newLibraryEntry(path, sourcePath, null, ClasspathUtils.createAccessRules(dependency), ClasspathUtils
                            .createClasspathAttributes(dependency), dependency.isExported());
                    result.add(entry);
                }
            }
        }
        return result.build();
    }

    private boolean tryCreatingLinkedResource(File dependencyFile, Builder<IClasspathEntry> result) {
        if (!dependencyFile.exists()) {
            IPath path = new Path("/" + dependencyFile.getPath());
            IResource member = this.eclipseProject.getProject().findMember(path);
            if (member != null) {
                IClasspathEntry entry = JavaCore.newLibraryEntry(member.getFullPath(), null, null);
                result.add(entry);
                return true;
            }
        }
        return false;
    }

    private List<IClasspathEntry> collectProjectDependencies() {
        Builder<IClasspathEntry> result = ImmutableList.builder();
        for (OmniEclipseProjectDependency dependency : this.gradleProject.getProjectDependencies()) {
            IPath path = new Path("/" + dependency.getPath());
            IClasspathEntry entry = JavaCore.newProjectEntry(path, ClasspathUtils.createAccessRules(dependency), true, ClasspathUtils.createClasspathAttributes(dependency), dependency.isExported());
            result.add(entry);
        }
        return result.build();
    }

    /**
     * Updates the classpath container of the target project based on the given Gradle model.
     * The container will be persisted so it does not have to be reloaded after the workbench is restarted.
     */
    public static void updateFromModel(IJavaProject eclipseProject, OmniEclipseProject gradleProject, Set<OmniEclipseProject> allGradleProjects, PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws JavaModelException {
        GradleClasspathContainerUpdater updater = new GradleClasspathContainerUpdater(eclipseProject, gradleProject, allGradleProjects);
        updater.updateClasspathContainer(persistentModel, monitor);
    }

    /**
     * Updates the classpath container from the state stored by the last call to {@link #updateFromModel(IJavaProject, OmniEclipseProject, IProgressMonitor)}.
     */
    public static boolean updateFromStorage(IJavaProject eclipseProject, IProgressMonitor monitor) throws JavaModelException {
        PersistentModel model = CorePlugin.modelPersistence().loadModel(eclipseProject.getProject());
        if (model.isPresent()) {
            setClasspathContainer(eclipseProject, model.getClasspath(), monitor);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resolves the classpath container to an empty list.
     */
    public static void clear(IJavaProject eclipseProject, IProgressMonitor monitor) throws JavaModelException {
        setClasspathContainer(eclipseProject, ImmutableList.<IClasspathEntry>of(), monitor);
    }

    private static void setClasspathContainer(IJavaProject eclipseProject, List<IClasspathEntry> classpathEntries, IProgressMonitor monitor) throws JavaModelException {
        IClasspathContainer classpathContainer = GradleClasspathContainer.newInstance(classpathEntries);
        JavaCore.setClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, new IJavaProject[]{eclipseProject}, new IClasspathContainer[]{classpathContainer}, monitor);
    }

}
