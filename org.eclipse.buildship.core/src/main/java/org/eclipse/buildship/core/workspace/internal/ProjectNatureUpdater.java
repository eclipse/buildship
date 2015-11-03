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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Updates the natures on the target project.
 */
public final class ProjectNatureUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_NATURES = "GRADLE_NATURES";

    private final IProject project;
    private final List<OmniEclipseProjectNature> natures;

    public ProjectNatureUpdater(IProject project, List<OmniEclipseProjectNature> natures) {
        this.project = Preconditions.checkNotNull(project);
        this.natures = Preconditions.checkNotNull(natures);
    }

    private void updateNatures(IProgressMonitor monitor) {
        monitor.beginTask("Updating project natures", 2);
        try {
            StringSetProjectProperty knownNatures = StringSetProjectProperty.from(project, PROJECT_PROPERTY_KEY_GRADLE_NATURES);
            addNewNaturesNewInGradleModel(knownNatures, new SubProgressMonitor(monitor, 1));
            removeNatureRemovedFromGradleModel(knownNatures, new SubProgressMonitor(monitor, 1));
        } catch (CoreException e) {
            CorePlugin.logger().error(String.format("Can't update project natures on %s.", project.getName()), e);
        } finally {
            monitor.done();
        }
    }

    private void addNewNaturesNewInGradleModel(StringSetProjectProperty knownNatures, IProgressMonitor monitor) {
        monitor.beginTask("Add new natures", natures.size());
        try {
            for (OmniEclipseProjectNature nature : natures) {
                String natureId = nature.getId();
                CorePlugin.workspaceOperations().addNature(project, natureId, new SubProgressMonitor(monitor, 1));
                knownNatures.add(natureId);
            }
        } finally {
            monitor.done();
        }
    }

    private void removeNatureRemovedFromGradleModel(StringSetProjectProperty knownNatures, IProgressMonitor monitor) throws CoreException {
        Set<String> knownNatureIds = knownNatures.get();
        monitor.beginTask("Remove old natures", knownNatureIds.size());
        try {
            for (String knownNatureId : knownNatureIds) {
                if (!natureIdExistsInGradleModel(knownNatureId)) {
                    CorePlugin.workspaceOperations().removeNature(project, knownNatureId, new SubProgressMonitor(monitor, 1));
                    knownNatures.remove(knownNatureId);
                } else {
                    monitor.worked(1);
                }
            }
        } finally {
            monitor.done();
        }
    }

    private boolean natureIdExistsInGradleModel(final String natureId) {
        return FluentIterable.from(natures).firstMatch(new Predicate<OmniEclipseProjectNature>() {

            @Override
            public boolean apply(OmniEclipseProjectNature nature) {
                return nature.getId().equals(natureId);
            }
        }).isPresent();
    }

    public static void update(IProject project, Optional<List<OmniEclipseProjectNature>> projectNatures, IProgressMonitor monitor) throws CoreException {
        List<OmniEclipseProjectNature> natures = projectNatures.isPresent() ? projectNatures.get() : Collections.<OmniEclipseProjectNature>emptyList();
        ProjectNatureUpdater updater = new ProjectNatureUpdater(project, natures);
        updater.updateNatures(monitor);
    }

}
