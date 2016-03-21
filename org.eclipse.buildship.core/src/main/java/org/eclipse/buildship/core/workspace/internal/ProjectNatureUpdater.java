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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Updates the natures on the target project.
 */
final class ProjectNatureUpdater {

    private static final String PROJECT_PROPERTY_KEY_GRADLE_NATURES = "natures";

    private final IProject project;
    private final ImmutableList<OmniEclipseProjectNature> natures;

    public ProjectNatureUpdater(IProject project, List<OmniEclipseProjectNature> natures) {
        this.project = Preconditions.checkNotNull(project);
        this.natures = ImmutableList.copyOf(natures);
    }

    private void updateNatures(IProgressMonitor monitor) {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        StringSetProjectProperty knownNatures = StringSetProjectProperty.from(this.project, PROJECT_PROPERTY_KEY_GRADLE_NATURES);
        removeNaturesRemovedFromGradleModel(knownNatures, progress.newChild(1));
        addNaturesNewInGradleModel(knownNatures, progress.newChild(1));
    }

    private void addNaturesNewInGradleModel(StringSetProjectProperty knownNatures, SubMonitor progress) {
        progress.setWorkRemaining(this.natures.size());
        Set<String> newNatureNames = Sets.newLinkedHashSet();
        for (OmniEclipseProjectNature nature : this.natures) {
            String natureId = nature.getId();
            CorePlugin.workspaceOperations().addNature(this.project, natureId, progress.newChild(1));
            newNatureNames.add(natureId);
        }
        knownNatures.set(newNatureNames);
    }

    private void removeNaturesRemovedFromGradleModel(StringSetProjectProperty knownNatures, SubMonitor progress) {
        Set<String> knownNatureIds = knownNatures.get();
        progress.setWorkRemaining(knownNatureIds.size());
        for (String knownNatureId : knownNatureIds) {
            SubMonitor childProgress = progress.newChild(1);
            if (!natureIdExistsInGradleModel(knownNatureId)) {
                CorePlugin.workspaceOperations().removeNature(this.project, knownNatureId, childProgress);
            }
        }
    }

    private boolean natureIdExistsInGradleModel(final String natureId) {
        return FluentIterable.from(this.natures).firstMatch(new Predicate<OmniEclipseProjectNature>() {

            @Override
            public boolean apply(OmniEclipseProjectNature nature) {
                return nature.getId().equals(natureId);
            }
        }).isPresent();
    }

    public static void update(IProject project, Optional<List<OmniEclipseProjectNature>> projectNatures, IProgressMonitor monitor) throws CoreException {
        List<OmniEclipseProjectNature> natures = projectNatures.or(Collections.<OmniEclipseProjectNature>emptyList());
        ProjectNatureUpdater updater = new ProjectNatureUpdater(project, natures);
        updater.updateNatures(monitor);
    }

}
