/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.util.file.RelativePathUtils;

/**
 * Updates the derived resource markers of a project. Stores the last state in the preferences, so
 * we can remove the derived markers later.
 *
 * @author Stefan Oehme
 */
final class DerivedResourcesUpdater {

    private final IProject project;
    private final IProject workspaceProject;
    private final OmniEclipseProject modelProject;

    private DerivedResourcesUpdater(IProject project, OmniEclipseProject modelProject) {
        this.project = Preconditions.checkNotNull(project);
        this.workspaceProject = Preconditions.checkNotNull(project);
        this.modelProject = Preconditions.checkNotNull(modelProject);
    }

    private void update(PersistentModelBuilder persistentModel, IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 3);
        try {
            IPath buildDirectoryPath = getBuildDirectoryPath();
            List<IPath> subprojectPaths = getNestedSubProjectFolderPaths(progress.newChild(1));
            List<IPath> derivedResources = getDerivedResources(buildDirectoryPath, subprojectPaths, progress.newChild(1));
            persistentModel.buildDir(buildDirectoryPath != null ?  buildDirectoryPath : new Path("build"));
            persistentModel.subprojectPaths(subprojectPaths);
            removePreviousMarkers(derivedResources,  persistentModel, progress.newChild(1));
            addNewMarkers(derivedResources, persistentModel, progress.newChild(1));
        } catch (CoreException e) {
            String message = String.format("Could not update derived resources on project %s.", this.project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private List<IPath> getDerivedResources(IPath possibleBuildDirectoryPath, List<IPath> subprojectPaths, SubMonitor progress) {
        List<IPath> derivedResources = Lists.<IPath>newArrayList(new Path(".gradle"));
        if (possibleBuildDirectoryPath != null) {
            derivedResources.add(possibleBuildDirectoryPath);
        }
        derivedResources.addAll(subprojectPaths);
        return derivedResources;
    }

    private List<IPath> getNestedSubProjectFolderPaths(SubMonitor progress) {
        List<IPath> subfolderPaths = Lists.newArrayList();
        final IPath parentPath = this.project.getLocation();
        for (OmniEclipseProject child : this.modelProject.getChildren()) {
            IPath childPath = Path.fromOSString(child.getProjectDirectory().getPath());
            if (parentPath.isPrefixOf(childPath)) {
                IPath relativePath = RelativePathUtils.getRelativePath(parentPath, childPath);
                subfolderPaths.add(relativePath);
            }
        }
        return subfolderPaths;
    }

    private void removePreviousMarkers(List<IPath> derivedResources, PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        PersistentModel previousModel = persistentModel.getPrevious();
        Collection<IPath> previouslyKnownDerivedResources = previousModel.isPresent() ? previousModel.getDerivedResources() : Collections.<IPath>emptyList();
        progress.setWorkRemaining(previouslyKnownDerivedResources.size());
        for (IPath resourcePath : previouslyKnownDerivedResources) {
            IResource resource = this.project.findMember(resourcePath);
            if (resource != null) {
                resource.setDerived(false, progress.newChild(1));
            } else {
                progress.worked(1);
            }
        }
    }

    private void addNewMarkers(List<IPath> derivedResources, PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(derivedResources.size());
        for (IPath resourcePath : derivedResources) {
            IResource resource = this.project.findMember(resourcePath);
            if (resource != null) {
                resource.setDerived(true, progress.newChild(1));
            } else {
                progress.worked(1);
            }
        }
        persistentModel.derivedResources(derivedResources);
    }

    /*
     * If no build directory is available via the TAPI, use 'build'. If build directory is
     * physically contained in the project, use that folder. If build directory is a linked
     * resource, use the linked folder. Optional.absent() if all of the above fail.
     */
    private IPath getBuildDirectoryPath() {
        OmniGradleProject gradleProject = this.modelProject.getGradleProject();
        Maybe<File> buildDirectory = gradleProject.getBuildDirectory();
        if (buildDirectory.isPresent() && buildDirectory.get() != null) {
            Path buildDirLocation = new Path(buildDirectory.get().getPath());
            return normalizeBuildDirectoryPath(buildDirLocation);
        } else {
            return new Path("build");
        }
    }

    private IPath normalizeBuildDirectoryPath(Path buildDirLocation) {
        IPath projectLocation = this.workspaceProject.getLocation();
        if (projectLocation.isPrefixOf(buildDirLocation)) {
            IPath relativePath = RelativePathUtils.getRelativePath(projectLocation, buildDirLocation);
            return relativePath;
        } else {
            for (OmniEclipseLinkedResource linkedResource : this.modelProject.getLinkedResources()) {
                if (buildDirLocation.toString().equals(linkedResource.getLocation())) {
                    return new Path(linkedResource.getName());
                }
            }
            return null;
        }
    }

    static void update(IProject workspaceProject, OmniEclipseProject project, PersistentModelBuilder persistentModel, IProgressMonitor monitor) {
        new DerivedResourcesUpdater(workspaceProject, project).update(persistentModel, monitor);
    }

}
