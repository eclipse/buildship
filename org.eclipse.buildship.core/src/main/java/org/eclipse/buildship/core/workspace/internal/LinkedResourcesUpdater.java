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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Updates the linked sources the target project.
 * <p/>
 * TODO (donat) Right now this class is called only on Java projects. But the concept of linked
 * resources is defined on all projects (~ on the IProject interface). Therefore the project update
 * functionality should be refactored such that all this class is executed on all projects, not just
 * on Java projects. Needs PR#140 to be merged before because it contains a
 * RefreshGradleClasspathContainerJob class where this class should be called.
 *
 * Note that currently, we only include linked resources that are folders.
 */
public final class LinkedResourcesUpdater {

    // magic number to select folders when checking OmniEclipseLinkedResource#getType()
    private static final String LINKED_RESOURCE_TYPE_FOLDER = "2";

    // value to assign to a linked folder if it comes from the Gradle model
    private static final QualifiedName RESOURCE_PROPERTY_FROM_GRADLE_MODEL = new QualifiedName(CorePlugin.PLUGIN_ID, "FROM_GRADLE_MODEL");

    private final IProject project;
    private final List<OmniEclipseLinkedResource> linkedResources;

    private LinkedResourcesUpdater(IProject project, List<OmniEclipseLinkedResource> linkedResources) {
        this.project = Preconditions.checkNotNull(project);
        this.linkedResources = Preconditions.checkNotNull(linkedResources);
    }

    private void updateLinkedResources(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Update linked resources", IProgressMonitor.UNKNOWN);
        try {
            removeOldLinkedResources();
            createNewLinkedResources();
        } finally {
            monitor.done();
        }
    }

    private void removeOldLinkedResources() throws CoreException {
        // check all potential linked folders which might have been created by this class and
        // delete the ones which are no longer part of the Gradle model
        List<IFolder> folders = collectLinkedFoldersNoLongerInGradleModel();
        deleteFolders(folders);
    }

    private List<IFolder> collectLinkedFoldersNoLongerInGradleModel() throws CoreException {
        return FluentIterable.of(this.project.members()).filter(IFolder.class).filter(new Predicate<IFolder>() {

            @Override
            public boolean apply(IFolder folder) {
                return folder.isLinked() && folder.getLocation() != null && !isLocationPartOfCurrentGradleModel(folder.getLocation().toFile()) && isFromGradleModel(folder);
            }

        }).toList();
    }

    private boolean isLocationPartOfCurrentGradleModel(final File location) {
        return FluentIterable.from(this.linkedResources).firstMatch(new Predicate<OmniEclipseLinkedResource>() {

            @Override
            public boolean apply(OmniEclipseLinkedResource linkedResource) {
                return isLinkedResourceFolder(linkedResource) && location.equals(new File(linkedResource.getLocation()));
            }
        }).isPresent();
    }

    private boolean isFromGradleModel(IFolder folder)  {
        try {
            return Optional.fromNullable(folder.getPersistentProperty(RESOURCE_PROPERTY_FROM_GRADLE_MODEL)).or("false").equals("true");
        } catch (CoreException e) {
            return false;
        }
    }

    private void deleteFolders(List<IFolder> folders) throws CoreException {
        for (IFolder folder : folders) {
            folder.delete(false, null);
        }
    }

    private void createNewLinkedResources() throws CoreException {
        Set<File> currentLinkedFolders = collectCurrentLinkedFolders();
        List<OmniEclipseLinkedResource> newLinkedResources = collectNewLinkedResources(currentLinkedFolders);
        createLinkedResources(newLinkedResources);
    }

    private Set<File> collectCurrentLinkedFolders() throws CoreException {
        return FluentIterable.of(this.project.members()).filter(IFolder.class).filter(new Predicate<IFolder>() {

            @Override
            public boolean apply(IFolder folder) {
                return folder.isLinked() && folder.getLocation() != null;
            }
        }).transform(new Function<IFolder, File>() {

            @Override
            public File apply(IFolder folder) {
                return folder.getLocation().toFile();
            }
        }).toSet();
    }

    private List<OmniEclipseLinkedResource> collectNewLinkedResources(final Set<File> currentLinkedFolders) throws CoreException {
        return FluentIterable.from(this.linkedResources).filter(new Predicate<OmniEclipseLinkedResource>() {

            @Override
            public boolean apply(OmniEclipseLinkedResource linkedResource) {
                return isLinkedResourceFolder(linkedResource) && !currentLinkedFolders.contains(new File(linkedResource.getLocation()));
            }
        }).toList();
    }

    private void createLinkedResources(List<OmniEclipseLinkedResource> linkedResources) throws CoreException {
        for (OmniEclipseLinkedResource linkedResource : linkedResources) {
            IPath resourcePath = new Path(linkedResource.getLocation());
            IFolder folder = toNewFolder(linkedResource.getName());
            folder.createLink(resourcePath, IResource.NONE, null);
            folder.setPersistentProperty(RESOURCE_PROPERTY_FROM_GRADLE_MODEL, "true");
        }
    }

    private IFolder toNewFolder(String folderName) throws CoreException {
        // if a folder with the same name already exists then create the location with a '_' appended to the name
        IFolder folder = this.project.getFolder(folderName);
        if (folder.exists()) {
            return toNewFolder(folderName + "_");
        } else {
            return folder;
        }
    }

    private boolean isLinkedResourceFolder(OmniEclipseLinkedResource linkedResource) {
        return linkedResource.getLocation() != null && linkedResource.getType().equals(LINKED_RESOURCE_TYPE_FOLDER);
    }

    public static void update(IProject project, List<OmniEclipseLinkedResource> linkedResources, IProgressMonitor monitor) throws CoreException {
        LinkedResourcesUpdater updater = new LinkedResourcesUpdater(project, linkedResources);
        updater.updateLinkedResources(monitor);
    }

}
