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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

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

    private void updateNatures(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, 2);
        StringSetProjectProperty knownNatures = StringSetProjectProperty.from(this.project, PROJECT_PROPERTY_KEY_GRADLE_NATURES);
        removeNaturesRemovedFromGradleModel(knownNatures, progress.newChild(1));
        addNaturesNewInGradleModel(knownNatures, progress.newChild(1));
    }

    private void addNaturesNewInGradleModel(StringSetProjectProperty knownNatures, SubMonitor progress) throws CoreException {
        Set<String> newNatureNames = Sets.newLinkedHashSet();
        for (OmniEclipseProjectNature nature : this.natures) {
            String natureId = nature.getId();
            if (natureRecognizedByEclipse(natureId)) {
                newNatureNames.add(natureId);
            }
        }
        addNatures(this.project, newNatureNames, progress);
        knownNatures.set(newNatureNames);
    }

    private void addNatures(IProject project, Set<String> natureIds, SubMonitor progress) throws CoreException {
        IProjectDescription description = project.getDescription();

        Set<String> newIds = Sets.newLinkedHashSet(Arrays.asList(description.getNatureIds()));
        newIds.addAll(natureIds);

        description.setNatureIds(newIds.toArray(new String[0]));
        project.setDescription(description, progress);
    }

    private boolean natureRecognizedByEclipse(String natureId) {
        return ResourcesPlugin.getWorkspace().getNatureDescriptor(natureId) != null;
    }

    private void removeNaturesRemovedFromGradleModel(StringSetProjectProperty knownNatures, SubMonitor progress) throws CoreException {
        Set<String> knownNatureIds = knownNatures.get();
        Set<String> naturesToRemove = Sets.newLinkedHashSet();
        for (String knownNatureId : knownNatureIds) {
            if (!natureIdExistsInGradleModel(knownNatureId)) {
                naturesToRemove.add(knownNatureId);
            }
        }
        removeNatures(this.project, naturesToRemove, progress);
    }

    private void removeNatures(IProject project, Set<String> natureIds, SubMonitor progress) throws CoreException {
        IProjectDescription description = project.getDescription();

        List<String> newIds = Lists.newArrayList(description.getNatureIds());
        newIds.removeAll(natureIds);

        description.setNatureIds(newIds.toArray(new String[0]));
        project.setDescription(description, progress);
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
