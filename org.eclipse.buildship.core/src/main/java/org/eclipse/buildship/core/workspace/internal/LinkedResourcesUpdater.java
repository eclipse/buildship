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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * Updates the linked sources of the target project.
 *
 * Note that currently, we only include linked resources that are folders.
 */
final class LinkedResourcesUpdater extends PersistentUpdater {
    private final IProject project;
    private final Map<String,OmniEclipseLinkedResource> linkedResources;

    private LinkedResourcesUpdater(IProject project, List<OmniEclipseLinkedResource> linkedResources) {
        super(project, "linkedResources");
        this.project = Preconditions.checkNotNull(project);
        this.linkedResources = FluentIterable.from(linkedResources).filter(new LinkedResourcesWithValidLocation()).uniqueIndex(new Function<OmniEclipseLinkedResource, String>() {

            @Override
            public String apply(OmniEclipseLinkedResource resource) {
                return resource.getLocation();
            }
        });
    }

    private void updateLinkedResources(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        removeOutdatedLinkedResources(progress.newChild(1));
        createLinkedResources(progress.newChild(1));
    }

    private void removeOutdatedLinkedResources(SubMonitor progress) throws CoreException {
        Collection<String> resourceNames = getKnownItems();
        progress.setWorkRemaining(resourceNames.size());
        for (String resourceName : resourceNames) {
            SubMonitor childProgress = progress.newChild(1);
            IFolder folder = this.project.getFolder(resourceName);
            if (shouldDelete(folder)) {
                folder.delete(false, childProgress);
            }
        }
    }

    private boolean shouldDelete(IFolder folder) {
        return linkedWithValidLocation(folder) && !partOfCurrentGradleModel(folder);
    }

    private boolean linkedWithValidLocation(IFolder folder) {
        return folder.exists() && folder.isLinked() && folder.getLocation() != null;
    }

    private boolean partOfCurrentGradleModel(IFolder folder) {
        return this.linkedResources.containsKey(folder.getLocation().toString());
    }

    private void createLinkedResources(SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(this.linkedResources.size());
        Set<String> resourceNames = Sets.newHashSet();
        for (OmniEclipseLinkedResource linkedResource : this.linkedResources.values()) {
            SubMonitor childProgress = progress.newChild(1);
            IFolder linkedResourceFolder = createLinkedResourceFolder(linkedResource.getName(), linkedResource, childProgress);
            resourceNames.add(projectRelativePath(linkedResourceFolder));
        }
        setKnownItems(resourceNames);
    }

    private IFolder createLinkedResourceFolder(String name, OmniEclipseLinkedResource linkedResource, SubMonitor progress) throws CoreException {
       IFolder folder = this.project.getFolder(name);
       IPath resourcePath = new Path(linkedResource.getLocation());
       FileUtils.ensureParentFolderHierarchyExists(folder);
       folder.createLink(resourcePath, IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, progress);
       return folder;
    }

    private String projectRelativePath(IFolder folder) {
        return folder.getFullPath().makeRelativeTo(this.project.getFullPath()).toPortableString();
    }

    public static void update(IProject project, List<OmniEclipseLinkedResource> linkedResources, IProgressMonitor monitor) throws CoreException {
        LinkedResourcesUpdater updater = new LinkedResourcesUpdater(project, linkedResources);
        updater.updateLinkedResources(monitor);
    }

    /**
     * Predicate matching to the {@link OmniEclipseLinkedResource} instances the updater can handle.
     */
    private static final class LinkedResourcesWithValidLocation implements Predicate<OmniEclipseLinkedResource> {

        // magic number to select folders when checking OmniEclipseLinkedResource#getType()
        private static final String LINKED_RESOURCE_TYPE_FOLDER = "2";

        @Override
        public boolean apply(OmniEclipseLinkedResource linkedResource) {
            return linkedResource.getLocation() != null && linkedResource.getType().equals(LINKED_RESOURCE_TYPE_FOLDER);
        }
    }

}
