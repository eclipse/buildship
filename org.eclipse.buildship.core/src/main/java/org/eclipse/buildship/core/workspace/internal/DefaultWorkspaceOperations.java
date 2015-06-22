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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.Closeables;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.workspace.ClasspathDefinition;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Default implementation of the {@link WorkspaceOperations} interface.
 */
public final class DefaultWorkspaceOperations implements WorkspaceOperations {

    @Override
    public ImmutableList<IProject> getAllProjects() {
        return ImmutableList.copyOf(ResourcesPlugin.getWorkspace().getRoot().getProjects());
    }

    @Override
    public void copyFileToContainer(IProgressMonitor progressMonitor, File file, IContainer destContainer, boolean recursively) throws FileNotFoundException, CoreException {
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, "Copy " + file.getName() + " to " + destContainer.getName(), 100);
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            subMonitor.setWorkRemaining(listFiles.length);
            for (File f : listFiles) {
                if (f.isDirectory()) {
                    IFolder newFolder = destContainer.getFolder(new Path(f.getName()));
                    newFolder.create(true, true, null);
                    if (recursively) {
                        copyFileToContainer(subMonitor.newChild(1), f, newFolder, recursively);
                    }
                } else {
                    copyFileToContainer(f, destContainer, subMonitor);
                }
            }
        } else {
            copyFileToContainer(file, destContainer, subMonitor);
        }
    }

    private void copyFileToContainer(File file, IContainer destContainer, SubMonitor subMonitor) throws FileNotFoundException, CoreException {
        Preconditions.checkArgument(!file.isDirectory(), "The given file must not be an directory"); //$NON-NLS-1$
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            IFile newFile = destContainer.getFile(new Path(file.getName()));
            newFile.create(fileInputStream, true, subMonitor.newChild(1));
        } finally {
            Closeables.closeQuietly(fileInputStream);
        }
    }

    @Override
    public Optional<IProject> findProjectByName(final String name) {
        return FluentIterable.from(getAllProjects()).firstMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return project.getName().equals(name);
            }
        });
    }

    @Override
    public Optional<IProjectDescription> findProjectInFolder(File location, IProgressMonitor monitor) {
        if (location == null || !location.exists()) {
            return Optional.absent();
        }

        File dotProjectFile = new File(location, ".project");
        if (!dotProjectFile.exists() || !dotProjectFile.isFile()) {
            return Optional.absent();
        }

        try {
            FileInputStream dotProjectStream = new FileInputStream(dotProjectFile);
            IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(dotProjectStream);
            description.setLocation(Path.fromOSString(location.getPath()));
            return Optional.of(description);
        } catch (Exception e) {
            String message = String.format("Cannot open existing Eclipse project from %s.", dotProjectFile.getAbsolutePath());
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    @Override
    public void deleteAllProjects(IProgressMonitor monitor) {
        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask("Delete all Eclipse projects from workspace", 100);
        try {
            List<IProject> allProjects = getAllProjects();
            for (IProject project : allProjects) {
                try {
                    // don't delete the project from the file system, only from the workspace
                    // moreover, force the removal even if the object is out-of-sync with the file
                    // system
                    project.delete(false, true, new SubProgressMonitor(monitor, 100 / allProjects.size()));
                } catch (Exception e) {
                    String message = String.format("Cannot delete project %s.", project.getName());
                    throw new GradlePluginsRuntimeException(message, e);
                }
            }
        } finally {
            monitor.done();
        }
    }

    @Override
    public IProject createProject(String name, File location, List<File> filteredSubFolders, List<String> natureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(natureIds);
        Preconditions.checkArgument(!name.isEmpty(), "Project name must not be empty.");
        Preconditions.checkArgument(location.exists(), String.format("Project location %s must exist.", location));
        Preconditions.checkArgument(location.isDirectory(), String.format("Project location %s must be a directory.", location));

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Create Eclipse project %s", name), 4 + natureIds.size());
        try {
            // make sure no project with the specified name already exists
            Preconditions.checkState(!findProjectByName(name).isPresent(), String.format("Workspace already contains project with name %s.", name));
            monitor.worked(1);

            // get an IProject instance and create the project
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProjectDescription projectDescription = workspace.newProjectDescription(name);
            projectDescription.setLocation(Path.fromOSString(location.getPath()));
            projectDescription.setComment(String.format("Project %s created by Buildship.", name));
            IProject project = workspace.getRoot().getProject(name);
            project.create(projectDescription, new SubProgressMonitor(monitor, 1));

            // attach filters to the project
            ResourceFilter.attachFilters(project, filteredSubFolders, new SubProgressMonitor(monitor, 1));

            // open the project
            project.open(new SubProgressMonitor(monitor, 1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            for (String natureId : natureIds) {
                addNature(project, natureId, new SubProgressMonitor(monitor, 1));
            }

            // return the created, open project
            return project;
        } catch (Exception e) {
            String message = String.format("Cannot create Eclipse project %s.", name);
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public IProject includeProject(IProjectDescription projectDescription, List<File> filteredSubFolders, List<String> extraNatureIds, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(projectDescription);
        Preconditions.checkNotNull(extraNatureIds);

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Include existing Eclipse project %s", projectDescription.getName()), 2 + extraNatureIds.size());
        try {
            // include the project in the workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = workspace.getRoot().getProject(projectDescription.getName());
            project.create(projectDescription, new SubProgressMonitor(monitor, 1));

            // attach filters to the project
            ResourceFilter.attachFilters(project, filteredSubFolders, new SubProgressMonitor(monitor, 1));

            // open the project
            project.open(new SubProgressMonitor(monitor, 1));

            // add project natures separately to trigger IProjectNature#configure
            // the project needs to be open while the natures are added
            for (String natureId : extraNatureIds) {
                addNature(project, natureId, new SubProgressMonitor(monitor, 1));
            }

            // return the included, open project
            return project;
        } catch (Exception e) {
            String message = String.format("Cannot include existing Eclipse project %s.", projectDescription.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public IJavaProject createJavaProject(IProject project, ClasspathDefinition classpath, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(project);
        Preconditions.checkNotNull(classpath);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Create Eclipse Java project %s", project.getName()), 17);
        try {
            // add Java nature
            addNature(project, JavaCore.NATURE_ID, new SubProgressMonitor(monitor, 2));

            // create the Eclipse Java project from the plain Eclipse project
            IJavaProject javaProject = JavaCore.create(project);
            monitor.worked(5);

            // set up initial classpath container on project
            setClasspathOnProject(javaProject, classpath, new SubProgressMonitor(monitor, 5));

            // set up output location
            IFolder outputFolder = createOutputFolder(project, new SubProgressMonitor(monitor, 1));
            javaProject.setOutputLocation(outputFolder.getFullPath(), new SubProgressMonitor(monitor, 1));

            // save the project configuration
            javaProject.save(new SubProgressMonitor(monitor, 2), true);

            // return the created Java project
            return javaProject;
        } catch (Exception e) {
            String message = String.format("Cannot create Eclipse Java project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override
    public void addNature(IProject project, String natureId, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Add nature %s to Eclipse project %s", natureId, project.getName()), 1);
        try {
            // get the description
            IProjectDescription description = project.getDescription();

            // abort if the project already has the nature applied
            List<String> currentNatureIds = ImmutableList.copyOf(description.getNatureIds());
            if (currentNatureIds.contains(natureId)) {
                return;
            }

            // add the nature to the project
            ImmutableList<String> newIds = ImmutableList.<String> builder().addAll(currentNatureIds).add(natureId).build();
            description.setNatureIds(newIds.toArray(new String[newIds.size()]));

            // save the updated description
            project.setDescription(description, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            String message = String.format("Cannot add nature %s to Eclipse project %s.", natureId, project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private IFolder createOutputFolder(IProject project, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Create output folder for Eclipse project %s", project.getName()), 1);
        try {
            IFolder outputFolder = project.getFolder("bin");
            if (!outputFolder.exists()) {
                outputFolder.create(true, true, new SubProgressMonitor(monitor, 1));
            }
            return outputFolder;
        } catch (Exception e) {
            String message = String.format("Cannot create output folder for Eclipse project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private void setClasspathOnProject(IJavaProject javaProject, ClasspathDefinition classpath, IProgressMonitor monitor) {
        monitor.beginTask(String.format("Configure sources and classpath for Eclipse project %s", javaProject.getProject().getName()), 10);
        try {
            // create a new holder for all classpath entries
            Builder<IClasspathEntry> entries = ImmutableList.builder();

            // add the library with the JRE dependencies
            entries.add(JavaCore.newContainerEntry(classpath.getJrePath()));
            monitor.worked(1);

            // add classpath definition of where to store the source/project/external dependencies,
            // the classpath
            // will be populated lazily by the org.eclipse.jdt.core.classpathContainerInitializer
            // extension point (see GradleClasspathContainerInitializer)
            entries.add(createGradleClasspathContainer());
            monitor.worked(1);

            // assign the whole classpath at once to the project
            List<IClasspathEntry> entriesArray = entries.build();
            javaProject.setRawClasspath(entriesArray.toArray(new IClasspathEntry[entriesArray.size()]), new SubProgressMonitor(monitor, 6));
        } catch (Exception e) {
            String message = String.format("Cannot configure sources and classpath for Eclipse project %s.", javaProject.getProject().getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

    private IClasspathEntry createGradleClasspathContainer() throws JavaModelException {
        // http://www-01.ibm.com/support/knowledgecenter/SSZND2_6.0.0/org.eclipse.jdt.doc.isv/guide/jdt_api_classpath.htm?cp=SSZND2_6.0.0%2F3-1-1-0-0-2
        Path containerPath = new Path(ClasspathDefinition.GRADLE_CLASSPATH_CONTAINER_ID);
        return JavaCore.newContainerEntry(containerPath, true);
    }

    @Override
    public void refresh(IProject project, IProgressMonitor monitor) {
        // validate arguments
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible(), "Project must be open.");

        monitor = MoreObjects.firstNonNull(monitor, new NullProgressMonitor());
        monitor.beginTask(String.format("Refresh Eclipse project %s", project.getName()), 1);
        try {
            project.refreshLocal(IProject.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
        } catch (Exception e) {
            String message = String.format("Cannot refresh Eclipse project %s.", project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            monitor.done();
        }
    }

}
