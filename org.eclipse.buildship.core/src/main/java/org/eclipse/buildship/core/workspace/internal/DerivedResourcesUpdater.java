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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Updates the derived resource markers of a project. Stores the last state in the preferences, so
 * we can remove the derived markers later.
 *
 * @author Stefan Oehme
 */
final class DerivedResourcesUpdater {
    private static final QualifiedName DERIVED_RESOURCES_KEY = new QualifiedName(CorePlugin.PLUGIN_ID, "derivedResources");

    private final IProject project;
    private final ImmutableList<String> derivedResources;

    private DerivedResourcesUpdater(IProject project, List<String> derivedResources) {
        this.project = Preconditions.checkNotNull(project);
        this.derivedResources = ImmutableList.copyOf(derivedResources);
    }

    private void update(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        try {
            removePreviousMarkers(progress.newChild(1));
            addNewMarkers(progress.newChild(1));
        } catch (CoreException e) {
            String message = String.format("Could not update derived resources on project %s.", this.project.getName());
            throw new GradlePluginsRuntimeException(message, e);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private void removePreviousMarkers(SubMonitor progress) throws CoreException {
        Collection<String> previouslyKnownDerivedResources = getKnownDerivedResources();
        progress.setWorkRemaining(previouslyKnownDerivedResources.size());
        for (String resourceName : previouslyKnownDerivedResources) {
            setDerived(resourceName, false, progress.newChild(1));
        }
    }

    private Collection<String> getKnownDerivedResources() throws CoreException {
        String serializedForm = this.project.getPersistentProperty(DERIVED_RESOURCES_KEY);
        if (serializedForm == null) {
            return Collections.emptyList();
        }
        return Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(serializedForm);
    }

    private void addNewMarkers(SubMonitor progress) throws CoreException {
        progress.setWorkRemaining(this.derivedResources.size());
        for (String resourceName : this.derivedResources) {
            setDerived(resourceName, true, progress.newChild(1));
        }
        setKnownDerivedResources(this.project, this.derivedResources);
    }

    private void setDerived(String resourceName, boolean derived, SubMonitor progress) throws CoreException {
        IResource derivedResource = this.project.findMember(resourceName);
        if (derivedResource != null) {
            derivedResource.setDerived(derived, progress);
        }
    }

    private void setKnownDerivedResources(IProject project, Collection<String> derivedResources) throws CoreException {
        project.setPersistentProperty(DERIVED_RESOURCES_KEY, Joiner.on(File.pathSeparator).join(derivedResources));
    }

    static void update(IProject project, List<String> derivedResources, IProgressMonitor monitor) {
        new DerivedResourcesUpdater(project, derivedResources).update(monitor);
    }

    static void clear(IProject project, IProgressMonitor monitor) {
        new DerivedResourcesUpdater(project, ImmutableList.<String>of()).update(monitor);
    }

}
