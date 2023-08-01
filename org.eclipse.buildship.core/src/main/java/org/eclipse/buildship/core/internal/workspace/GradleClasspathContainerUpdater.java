/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

import com.google.common.base.Preconditions;
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

import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.CoreTraceScopes;
import org.eclipse.buildship.core.internal.Logger;
import org.eclipse.buildship.core.internal.TraceScope;
import org.eclipse.buildship.core.internal.preferences.ClasspathConverter;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.util.classpath.ClasspathUtils;

/**
 * Updates the classpath container of the target project.
 * <p/>
 * The update is triggered via
 * {@link #updateFromModel(IJavaProject, EclipseProject, Set, IProgressMonitor, ProjectContext)}.
 * The method executes synchronously and unprotected, without thread synchronization or job
 * scheduling.
 * <p/>
 * The update logic composes a new classpath container containing all project and external
 * dependencies defined in the Gradle model. At the end of the execution the old classpath container
 * is replaced by the one being created.
 * <p/>
 * If an invalid external dependency is received (anything else, than a folder, {@code .jar} file or
 * {@code .zip} file) the given entry is omitted from the classpath container. Due to performance
 * reasons only the file extension is checked.
 */
final class GradleClasspathContainerUpdater {

    // Pattern from https://github.com/gradle/gradle/blob/2546d790db1a8b263d34c36669ae39a4537c922c/subprojects/ide/src/main/java/org/gradle/plugins/ide/internal/resolver/UnresolvedIdeDependencyHandler.java#L42
    private static final String UNRESOLVED_DEPENDENCY_NAME_PREFIX  = "unresolved dependency - ";
    private static final Pattern UNRESOLVED_DEPENDENCY_NAME_PATTERN  = Pattern.compile("^([^ ]+) ([^ ]+) ([^ ]+)$");

    private final IJavaProject eclipseProject;
    private final EclipseProject gradleProject;
    private final Map<File, EclipseProject> projectDirToProject;
    private final ProjectContext projectContext;

    private GradleClasspathContainerUpdater(IJavaProject eclipseProject, EclipseProject gradleProject, Iterable<EclipseProject> allGradleProjects, ProjectContext projectContext) {
        this.projectContext = projectContext;
        this.eclipseProject = Preconditions.checkNotNull(eclipseProject);
        this.gradleProject = Preconditions.checkNotNull(gradleProject);
        this.projectDirToProject = Maps.newHashMap();

        for (EclipseProject project : allGradleProjects) {
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

        boolean hasExportedEntry = externalDependencies.stream().anyMatch(IClasspathEntry::isExported);

        // Gradle distributions <2.5 rely on exports to define the project classpath. Unfortunately
        // that logic is broken if dependency excludes are defined in the build scripts. To work
        // around that, external dependencies must be defined before project dependencies. For more
        // details, visit Bug 473348.
        if (hasExportedEntry) {
            return ImmutableList.<IClasspathEntry> builder().addAll(externalDependencies).addAll(projectDependencies).build();
        } else {
            return ImmutableList.<IClasspathEntry> builder().addAll(projectDependencies).addAll(externalDependencies).build();
        }
    }

    private List<IClasspathEntry> collectExternalDependencies() {
        Builder<IClasspathEntry> result = ImmutableList.builder();
        for (EclipseExternalDependency dependency : this.gradleProject.getClasspath()) {
            File dependencyFile = dependency.getFile();
            boolean linkedResourceCreated = tryCreatingLinkedResource(dependencyFile, result);
            if (!linkedResourceCreated) {
                // Add error marker for unresolved dependencies
                if (this.projectContext != null && dependencyFile.getName().startsWith(UNRESOLVED_DEPENDENCY_NAME_PREFIX)) {
                    String coordinates = dependencyFile.getName().substring(UNRESOLVED_DEPENDENCY_NAME_PREFIX.length());
                    Matcher m = UNRESOLVED_DEPENDENCY_NAME_PATTERN.matcher(coordinates);
                    if (m.matches()) {
                        String groupId = m.group(1);
                        String artifactId = m.group(2);
                        String version = m.group(3);
                        this.projectContext.error("Unresolved dependency: " + groupId + ":" + artifactId + ":" + version, null);
                    } else {
                        this.projectContext.error("Unresolved dependency: " + coordinates, null);
                    }
                    continue;
                }
                String dependencyName = dependencyFile.getName();
                // Eclipse only accepts folders and archives as external dependencies (but not, for
                // example, a DLL)
                if (dependencyFile.isDirectory() || hasAcceptedSuffix(dependencyName)) {
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

    private boolean hasAcceptedSuffix(String dependencyName) {
       String name = dependencyName.toLowerCase();
       return name.endsWith(".jar") || name.endsWith(".rar") || name.endsWith(".zip");
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
        for (EclipseProjectDependency dependency : this.gradleProject.getProjectDependencies()) {
            IPath path = new Path("/" + dependency.getPath());
            IClasspathEntry entry = JavaCore
                    .newProjectEntry(path, ClasspathUtils.createAccessRules(dependency), true, ClasspathUtils.createClasspathAttributes(dependency), dependency.isExported());
            result.add(entry);
        }
        return result.build();
    }

    /**
     * Updates the classpath container of the target project based on the given Gradle model. The
     * container will be persisted so it does not have to be reloaded after the workbench is
     * restarted.
     */
    public static void updateFromModel(IJavaProject eclipseProject, EclipseProject gradleProject, Iterable<EclipseProject> allGradleProjects,
            PersistentModelBuilder persistentModel, IProgressMonitor monitor, ProjectContext context) throws JavaModelException {
        GradleClasspathContainerUpdater updater = new GradleClasspathContainerUpdater(eclipseProject, gradleProject, allGradleProjects, context);
        updater.updateClasspathContainer(persistentModel, monitor);
    }

    /**
     * Updates the classpath container from the state stored by the last call to
     * {@link #updateFromModel(IJavaProject, EclipseProject, IProgressMonitor, ProjectContext)}.
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
        setClasspathContainer(eclipseProject, ImmutableList.<IClasspathEntry> of(), monitor);
    }

    private static void setClasspathContainer(IJavaProject eclipseProject, List<IClasspathEntry> classpathEntries, IProgressMonitor monitor) throws JavaModelException {
        traceClasspathEntries(eclipseProject, classpathEntries);
        IClasspathContainer classpathContainer = GradleClasspathContainer.newInstance(classpathEntries);
        JavaCore.setClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, new IJavaProject[] { eclipseProject }, new IClasspathContainer[] { classpathContainer }, monitor);
    }

    private static void traceClasspathEntries(IJavaProject eclipseProject, List<IClasspathEntry> classpathEntries) {
        Logger logger = CorePlugin.logger();
        TraceScope scope = CoreTraceScopes.CLASSPATH;
        if (logger.isScopeEnabled(scope)) {
            IPath path = eclipseProject.getPath().append(GradleClasspathContainer.CONTAINER_PATH);
            String entries = new ClasspathConverter(eclipseProject).toXml(classpathEntries);
            logger.trace(scope, path + "=" + entries);
        }
    }

}
