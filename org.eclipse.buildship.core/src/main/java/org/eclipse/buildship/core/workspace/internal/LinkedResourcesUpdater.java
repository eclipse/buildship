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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.omnimodel.OmniEclipseLinkedResource;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * Updates the linked sources of the target project.
 *
 * Note that currently we only include linked resources that are folders.
 */
final class LinkedResourcesUpdater {

    private final IProject project;
    private final ImmutableList<OmniEclipseLinkedResource> resources;
    private final ImmutableSet<IPath> resourcePaths;

    private LinkedResourcesUpdater(IProject project, List<OmniEclipseLinkedResource> linkedResources) {
        this.project = Preconditions.checkNotNull(project);
        ImmutableList.Builder<OmniEclipseLinkedResource> resources = ImmutableList.builder();
        ImmutableSet.Builder<IPath> resourcePaths = ImmutableSet.builder();
        for (OmniEclipseLinkedResource linkedResource : linkedResources) {
            if (isSupportedLinkedResource(linkedResource)) {
                resources.add(linkedResource);
                resourcePaths.add(new Path(linkedResource.getLocation()));
            }
        }
        this.resources = resources.build();
        this.resourcePaths = resourcePaths.build();
    }

    private static boolean isSupportedLinkedResource(OmniEclipseLinkedResource linkedResource) {
        // linked resources with locationUri set are not supported
        return linkedResource.getLocation() != null;
    }

    private void updateLinkedResources(PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        removeOutdatedLinkedResources(persistentModel, progress.newChild(1));
        createLinkedResources(persistentModel, progress.newChild(1));
    }

    private void removeOutdatedLinkedResources(PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        PersistentModel previousModel = persistentModel.getPrevious();
        Collection<IPath> linkedPaths = previousModel.isPresent() ? previousModel.getLinkedResources() : Collections.<IPath>emptyList();
        progress.setWorkRemaining(linkedPaths.size());
        for (IPath linkedPath : linkedPaths) {
            SubMonitor childProgress = progress.newChild(1);
            IResource linkedResource = this.project.findMember(linkedPath);
            if (shouldDelete(linkedResource)) {
                linkedResource.delete(false, childProgress);
            }
        }
    }

    private boolean shouldDelete(IResource resource) {
        return resource != null && linkedWithValidLocation(resource) && !partOfCurrentGradleModel(resource);
    }

    private boolean linkedWithValidLocation(IResource resource) {
        return resource.exists() && resource.isLinked() && resource.getLocation() != null;
    }

    private boolean partOfCurrentGradleModel(IResource resource) {
        return this.resourcePaths.contains(resource.getProjectRelativePath());
    }

    private void createLinkedResources(PersistentModelBuilder persistentModel, SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(this.resources.size());
        Set<IPath> linkedPaths = Sets.newHashSet();
        for (OmniEclipseLinkedResource linkedFile : this.resources) {
            SubMonitor childProgress = progress.newChild(1);
            IResource file = createLinkedResource(linkedFile, childProgress);
            linkedPaths.add(file.getProjectRelativePath());
        }
        persistentModel.linkedResources(linkedPaths);
    }

    private IResource createLinkedResource(OmniEclipseLinkedResource linkedResource, SubMonitor progress) throws CoreException {
        String name = linkedResource.getName();
        String type = linkedResource.getType();
        IPath path = new Path(linkedResource.getLocation());
        if ("1".equals(type)) { // magic number for linked files
            IFile file = this.project.getFile(name);
            FileUtils.ensureParentFolderHierarchyExists(file);
            file.createLink(path, IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, progress);
            return file;
        } else if ("2".equals(type)) { // magic number for linked folders
            IFolder folder = this.project.getFolder(name);
            FileUtils.ensureParentFolderHierarchyExists(folder);
            folder.createLink(path, IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, progress);
            return folder;
        } else {
            throw new GradlePluginsRuntimeException("Unknows linked resource type: " + type);
        }
    }

    public static void update(IProject project, List<OmniEclipseLinkedResource> linkedResources, PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws CoreException {
        LinkedResourcesUpdater updater = new LinkedResourcesUpdater(project, linkedResources);
        updater.updateLinkedResources(persistentModel, monitor);
    }
}
