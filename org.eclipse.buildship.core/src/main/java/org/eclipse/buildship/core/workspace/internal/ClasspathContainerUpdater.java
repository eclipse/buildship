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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniClasspathEntry;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates the classpath container of the target project.
 * <p/>
 * The update is triggered via {@link #updateFromModel(IJavaProject, OmniEclipseProject, IProgressMonitor)}.
 * The method executes synchronously and unprotected, without thread synchronization or job scheduling.
 * <p/>
 * The update logic composes a new classpath container containing all project and external
 * dependencies defined in the Gradle model. At the end of the execution the old classpath
 * container is replaced by the one being created.
 * <p/>
 * If an invalid external dependency is received (anything else, than a {@code .jar} file
 * or {@code .zip} file) the given entry is omitted from the classpath container. Due to
 * performance reasons only the file extension is checked.
 */
final class ClasspathContainerUpdater {

    private final IJavaProject eclipseProject;
    private final OmniEclipseProject gradleProject;

    private ClasspathContainerUpdater(IJavaProject eclipseProject, OmniEclipseProject gradleProject) {
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
    }

    private void updateClasspathContainer(IProgressMonitor monitor) throws JavaModelException {
        ImmutableList<IClasspathEntry> containerEntries = collectClasspathContainerEntries();
        setClasspathContainer(this.eclipseProject, containerEntries, monitor);
        ClasspathContainerPersistence.save(this.eclipseProject, containerEntries);
    }

    private ImmutableList<IClasspathEntry> collectClasspathContainerEntries() {
        // project dependencies
        List<IClasspathEntry> projectDependencies = FluentIterable.from(this.gradleProject.getProjectDependencies())
                .transform(new Function<OmniEclipseProjectDependency, IClasspathEntry>() {

                    @Override
                    public IClasspathEntry apply(OmniEclipseProjectDependency dependency) {
                        OmniEclipseProject dependentProject = ClasspathContainerUpdater.this.gradleProject.getRoot()
                                .tryFind(Specs.eclipseProjectMatchesProjectDir(dependency.getTargetProjectDir())).get();
                        String actualName = CorePlugin.workspaceOperations().normalizeProjectName(dependentProject.getName(), dependentProject.getProjectDirectory());
                        Path path = new Path("/" + actualName);
                        return JavaCore.newProjectEntry(path, null, true, getClasspathAttributes(dependency), dependency.isExported());
                    }
                }).toList();

        // external dependencies
        List<IClasspathEntry> externalDependencies = FluentIterable.from(this.gradleProject.getExternalDependencies()).filter(new Predicate<OmniExternalDependency>() {

            @Override
            public boolean apply(OmniExternalDependency dependency) {
                File file = dependency.getFile();
                String name = file.getName();
                // Eclipse only accepts folders and archives as external dependencies (but not, for example, a DLL)
                return file.isDirectory() || name.endsWith(".jar") || name.endsWith(".zip");
            }
        }).transform(new Function<OmniExternalDependency, IClasspathEntry>() {

            @Override
            public IClasspathEntry apply(OmniExternalDependency dependency) {
                IPath file = org.eclipse.core.runtime.Path.fromOSString(dependency.getFile().getAbsolutePath());
                IPath sources = dependency.getSource() != null ? org.eclipse.core.runtime.Path.fromOSString(dependency.getSource().getAbsolutePath()) : null;
                return JavaCore.newLibraryEntry(file, sources, null, null, getClasspathAttributes(dependency), dependency.isExported());
            }
        }).toList();

        // return all dependencies as a joined list - The order of the dependencies is important see Bug 473348
        return ImmutableList.<IClasspathEntry>builder().addAll(externalDependencies).addAll(projectDependencies).build();
    }

    private IClasspathAttribute[] getClasspathAttributes(OmniClasspathEntry entry) {
        IClasspathAttribute[] classpathAttributes = new IClasspathAttribute[entry.getClasspathAttributes().size()];
        List<OmniClasspathAttribute> attributes = entry.getClasspathAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            OmniClasspathAttribute attribute = attributes.get(i);
            classpathAttributes[i] = JavaCore.newClasspathAttribute(attribute.getName(), attribute.getValue());
        }
        return classpathAttributes;
    }

    /**
     * Updates the classpath container of the target project based on the given Gradle model.
     * The container will be persisted so it does not have to be reloaded after the workbench is restarted.
     *
     * @param eclipseProject         the target project to update the classpath container on
     * @param gradleProject          the Gradle model to read the dependencies from
     * @param monitor                the monitor to report progress on
     * @throws JavaModelException if the container assignment fails
     */
    public static void updateFromModel(IJavaProject eclipseProject, OmniEclipseProject gradleProject, IProgressMonitor monitor) throws JavaModelException {
        ClasspathContainerUpdater updater = new ClasspathContainerUpdater(eclipseProject, gradleProject);
        updater.updateClasspathContainer(monitor);
    }

    /**
     * Updates the classpath container from the state stored by the last call to {@link #updateFromModel(IJavaProject, OmniEclipseProject, IProgressMonitor)}.
     *
     * @param eclipseProject the target project to update the classpath container on
     * @param monitor the monitor to report progress on
     * @return true if the container could be loaded, false if the container remains uninitialized
     * @throws JavaModelException if the classpath cannot be assigned
     */
    public static boolean updateFromStorage(IJavaProject eclipseProject, IProgressMonitor monitor) throws JavaModelException {
        Optional<List<IClasspathEntry>> storedClasspath = ClasspathContainerPersistence.load(eclipseProject);
        if (storedClasspath.isPresent()) {
            setClasspathContainer(eclipseProject, storedClasspath.get(), monitor);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resolves the classpath container to an empty list.
     *
     * @param eclipseProject      the target project to update the classpath container on
     * @param monitor             the monitor to report progress on
     * @throws JavaModelException if the container assignment fails
     */
    public static void clear(IJavaProject eclipseProject, IProgressMonitor monitor) throws JavaModelException {
        setClasspathContainer(eclipseProject, ImmutableList.<IClasspathEntry>of(), monitor);
    }

    private static void setClasspathContainer(IJavaProject eclipseProject, List<IClasspathEntry> classpathEntries, IProgressMonitor monitor) throws JavaModelException {
        IClasspathContainer classpathContainer = GradleClasspathContainer.newInstance(classpathEntries);
        JavaCore.setClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, new IJavaProject[]{eclipseProject}, new IClasspathContainer[]{classpathContainer}, monitor);
    }

}
