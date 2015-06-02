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

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;

/**
 * Initializes the classpath of each Eclipse workspace project that has a Gradle nature with the
 * source/project/external dependencies of the underlying Gradle project.
 * <p/>
 * When this initializer is invoked, it looks up the {@link OmniEclipseProject} for the given
 * Eclipse workspace project, takes all the found sources, project dependencies and external
 * dependencies, and assigns them to the {@link ClasspathDefinition#GRADLE_CLASSPATH_CONTAINER_ID}
 * classpath container.
 * <p/>
 * This initializer is assigned to the projects via the
 * {@code org.eclipse.jdt.core.classpathContainerInitializer} extension point.
 * <p/>
 * The initialization is scheduled as a job, to not block the IDE upon startup.
 */
public final class GradleClasspathContainerInitializer extends ClasspathContainerInitializer {

    private static final String CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL = "FROM_GRADLE_MODEL";

    /**
     * Looks up the {@link OmniEclipseProject} for the target project, takes all external Jar
     * dependencies and assigns them to the classpath container with id
     * {@link ClasspathDefinition#GRADLE_CLASSPATH_CONTAINER_ID}.
     */
    @Override
    public void initialize(final IPath containerPath, final IJavaProject javaProject) throws CoreException {
        new ToolingApiWorkspaceJob("Initialize Gradle classpath for project '" + javaProject.getElementName() + "'") {

            @Override
            protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
                monitor.beginTask("Initializing classpath", 100);

                // use the same rule as the ProjectImportJob to do the initialization
                IJobManager manager = Job.getJobManager();
                IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                manager.beginRule(workspaceRoot, monitor);
                try {
                    internalInitialize(containerPath, javaProject, monitor);
                } finally {
                    manager.endRule(workspaceRoot);
                }
            }
        }.schedule();
    }

    private void internalInitialize(IPath containerPath, IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
        Optional<OmniEclipseProject> eclipseProject = findEclipseProject(project.getProject());
        if (eclipseProject.isPresent()) {
            // refresh the project
            CorePlugin.workspaceOperations().refresh(project.getProject(), new SubProgressMonitor(monitor, 25));

            // update source folders
            List<IClasspathEntry> sourceFolders = collectSourceFolders(eclipseProject.get(), project);
            updateSourceFoldersInClasspath(sourceFolders, project, new SubProgressMonitor(monitor, 25));

            // update project/external dependencies
            ImmutableList<IClasspathEntry> dependencies = collectDependencies(eclipseProject.get());
            setClasspathContainer(dependencies, project, containerPath, new SubProgressMonitor(monitor, 25));

            // save the updated project
            project.save(new SubProgressMonitor(monitor, 25), true);
        } else {
            throw new GradlePluginsRuntimeException(String.format("Cannot find Eclipse project model for project %s.", project.getProject()));
        }
    }

    private Optional<OmniEclipseProject> findEclipseProject(IProject project) {
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(configuration.getRequestAttributes());
        return eclipseGradleBuild.getRootEclipseProject().tryFind(Specs.eclipseProjectMatchesProjectPath(configuration.getProjectPath()));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> noProgressListeners = ImmutableList.of();
        List<org.gradle.tooling.events.ProgressListener> noTypedProgressListeners = ImmutableList.of();
        CancellationToken cancellationToken = GradleConnector.newCancellationTokenSource().token();
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, noProgressListeners,
                noTypedProgressListeners, cancellationToken);
        ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
    }

    private List<IClasspathEntry> collectSourceFolders(OmniEclipseProject gradleProject, final IJavaProject workspaceProject) {
        return FluentIterable.from(gradleProject.getSourceDirectories()).transform(new Function<OmniEclipseSourceDirectory, IClasspathEntry>() {

            @Override
            public IClasspathEntry apply(OmniEclipseSourceDirectory directory) {
                IFolder sourceDirectory = workspaceProject.getProject().getFolder(Path.fromOSString(directory.getPath()));
                FileUtils.ensureFolderHierarchyExists(sourceDirectory);
                IPackageFragmentRoot root = workspaceProject.getPackageFragmentRoot(sourceDirectory);
                IClasspathAttribute fromGradleModel = JavaCore.newClasspathAttribute(CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL, "true");
                // @formatter:off
                return JavaCore.newSourceEntry(root.getPath(),
                        new IPath[] {},                               // include all files
                        new IPath[] {},                               // don't exclude anything
                        null,                                         // use the same output folder as defined on the project
                        new IClasspathAttribute[] { fromGradleModel } // the source folder is loaded from the current Gradle model
                    );
                // @formatter:on
            }
        }).toList();
    }

    private void updateSourceFoldersInClasspath(List<IClasspathEntry> gradleModelSourceFolders, IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
        // collect all existing source folders
        ImmutableList<IClasspathEntry> rawClasspath = ImmutableList.copyOf(project.getRawClasspath());

        final ImmutableSet<String> gradleSourcePaths = FluentIterable.from(gradleModelSourceFolders).transform(new Function<IClasspathEntry, String>() {

            @Override
            public String apply(IClasspathEntry entry) {
                return entry.getPath().toString();
            }
        }).toSet();

        // keep the source folders not coming from the Gradle model
        final List<IClasspathEntry> sourceFoldersOutsideFromGradle = FluentIterable.from(rawClasspath).filter(new Predicate<IClasspathEntry>() {

            @Override
            public boolean apply(IClasspathEntry entry) {
                // if a manually-created source folder also exists in the Gradle model too, then
                // treat it as a Gradle source folder
                if (gradleSourcePaths.contains(entry.getPath().toString())) {
                    return false;
                }

                // marked with extra attribute
                for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
                    if (attribute.getName().equals(CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL) && attribute.getValue().equals("true")) {
                        return false;
                    }
                }
                return true;
            }
        }).toList();

        // new classpath = current source folders from the Gradle model + the previous ones defined
        // manually
        ImmutableList<IClasspathEntry> newRawClasspathEntries = ImmutableList.<IClasspathEntry> builder().addAll(gradleModelSourceFolders).addAll(sourceFoldersOutsideFromGradle)
                .build();
        IClasspathEntry[] newRawClasspath = newRawClasspathEntries.toArray(new IClasspathEntry[newRawClasspathEntries.size()]);
        project.setRawClasspath(newRawClasspath, monitor);
    }

    private ImmutableList<IClasspathEntry> collectDependencies(final OmniEclipseProject gradleProject) {
        // project dependencies
        List<IClasspathEntry> projectDependencies = FluentIterable.from(gradleProject.getProjectDependencies())
                .transform(new Function<OmniEclipseProjectDependency, IClasspathEntry>() {

                    @Override
                    public IClasspathEntry apply(OmniEclipseProjectDependency dependency) {
                        OmniEclipseProject dependentProject = gradleProject.getRoot().tryFind(Specs.eclipseProjectMatchesProjectPath(dependency.getTargetProjectPath())).get();
                        IPath path = new Path("/" + dependentProject.getName());
                        return JavaCore.newProjectEntry(path, dependency.isExported());
                    }
                }).toList();

        // external dependencies
        List<IClasspathEntry> externalDependencies = FluentIterable.from(gradleProject.getExternalDependencies()).filter(new Predicate<OmniExternalDependency>() {

            @Override
            public boolean apply(OmniExternalDependency dependency) {
                // Eclipse only accepts archives as external dependencies (but not, for example, a
                // DLL)
                String name = dependency.getFile().getName();
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        }).transform(new Function<OmniExternalDependency, IClasspathEntry>() {

            @Override
            public IClasspathEntry apply(OmniExternalDependency dependency) {
                IPath jar = org.eclipse.core.runtime.Path.fromOSString(dependency.getFile().getAbsolutePath());
                IPath sourceJar = dependency.getSource() != null ? org.eclipse.core.runtime.Path.fromOSString(dependency.getSource().getAbsolutePath()) : null;
                return JavaCore.newLibraryEntry(jar, sourceJar, null, dependency.isExported());
            }
        }).toList();

        // return all dependencies as a joined list
        return ImmutableList.<IClasspathEntry> builder().addAll(projectDependencies).addAll(externalDependencies).build();
    }

    private void setClasspathContainer(List<IClasspathEntry> classpathEntries, IJavaProject project, IPath containerPath, IProgressMonitor monitor) throws JavaModelException {
        org.eclipse.core.runtime.Path classpathContainerPath = new org.eclipse.core.runtime.Path(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID);
        IClasspathContainer classpathContainer = new GradleClasspathContainer("Project and External Dependencies", classpathContainerPath, classpathEntries);
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { classpathContainer }, monitor);
    }

    /**
     * {@code IClasspathContainer} to describe the external dependencies.
     */
    private static final class GradleClasspathContainer implements IClasspathContainer {

        private final String containerName;
        private final org.eclipse.core.runtime.Path path;
        private final IClasspathEntry[] classpathEntries;

        private GradleClasspathContainer(String containerName, org.eclipse.core.runtime.Path path, List<IClasspathEntry> classpathEntries) {
            this.containerName = Preconditions.checkNotNull(containerName);
            this.path = Preconditions.checkNotNull(path);
            this.classpathEntries = Iterables.toArray(classpathEntries, IClasspathEntry.class);
        }

        @Override
        public String getDescription() {
            return this.containerName;
        }

        @Override
        public IPath getPath() {
            return this.path;
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return this.classpathEntries;
        }

        @Override
        public int getKind() {
            return IClasspathContainer.K_APPLICATION;
        }

    }

}
