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
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Updates the linked sources the target project.
 *
 * TODO (donat) Right now this class is called only on Java projects. But the concept of linked
 * resources is defined on all projects (~ on the IProject interface). Therefore the project update
 * functionality should be refactored such that all this class is executed on all projects, not just
 * on Java projects. Needs PR#140 to be merged before because it contains a
 * RefreshGradleClasspathContainerJob class where this class should be called.
 */
public final class LinkedResourcesUpdater {

    // magic number to select folders when checking OmniEclipseLinkedResource#getType()
    private static final String LINKED_RESOURCE_TYPE_FOLDER = "2";

    private final IProject project;
    private final List<OmniEclipseLinkedResource> linkedResources;

    private LinkedResourcesUpdater(IProject project, List<OmniEclipseLinkedResource> linkedResources) {
        this.project = Preconditions.checkNotNull(project);
        this.linkedResources = Preconditions.checkNotNull(linkedResources);
    }

    private void updateLinkedResources(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Update linked resources", IProgressMonitor.UNKNOWN);
        try {
            // todo (donat) delete linked resources which were but are no longer part of the model
            Set<File> currentLinkedResourcesLocations = collectLinkedResourcesLocations();
            List<OmniEclipseLinkedResource> newLinkedResources = collectNewLinkedResources(currentLinkedResourcesLocations);
            createLinkedResources(newLinkedResources);
        } finally {
            monitor.done();
        }
    }

    private Set<File> collectLinkedResourcesLocations() throws CoreException {
        return FluentIterable.of(this.project.members()).filter(new Predicate<IResource>() {

            @Override
            public boolean apply(IResource folder) {
                return folder.isLinked();
            }
        }).transform(new Function<IResource, File>() {

            @Override
            public File apply(IResource resource) {
                return resource.getLocation().toFile();
            }
        }).toSet();
    }

    private List<OmniEclipseLinkedResource> collectNewLinkedResources(final Set<File> existingLocations) {
        return FluentIterable.from(this.linkedResources).filter(new Predicate<OmniEclipseLinkedResource>() {

            @Override
            public boolean apply(OmniEclipseLinkedResource resource) {
                // currently, we only include linked resources that are folders
                return resource.getLocation() != null && resource.getType().equals(LINKED_RESOURCE_TYPE_FOLDER) && !existingLocations.contains(new File(resource.getLocation()));
            }
        }).toList();
    }

    private void createLinkedResources(List<OmniEclipseLinkedResource> linkedResources) throws CoreException {
        for (OmniEclipseLinkedResource linkedResource : linkedResources) {
            IPath resourcePath = new Path(linkedResource.getLocation());
            IFolder folder = toNewFolder(linkedResource.getName());
            folder.createLink(resourcePath, IResource.NONE, null);
        }
    }

    private IFolder toNewFolder(String folderName) throws CoreException {
        // if a folder with the same name already exists then create the location with a '_'
        // appended to the name
        IFolder folder = this.project.getFolder(folderName);
        if (folder.exists()) {
            return toNewFolder(folderName + "_");
        } else {
            return folder;
        }
    }

    public static void update(IProject project, List<OmniEclipseLinkedResource> linkedResources, IProgressMonitor monitor) throws CoreException {
        LinkedResourcesUpdater updater = new LinkedResourcesUpdater(project, linkedResources);
        updater.updateLinkedResources(monitor);
    }

}
