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
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectNature;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.workspace.internal.ManagedModelMergingStrategy.Result;

/**
 * Updates the natures on the target project.
 */
final class ProjectNatureUpdater {

    private ProjectNatureUpdater() {
    }

    public static void update(IProject project, Optional<List<OmniEclipseProjectNature>> projectNatures, PersistentModelBuilder persistentModel, IProgressMonitor monitor) throws CoreException {
        PersistentModel previousPersistentModel = persistentModel.getPrevious();
        Set<String> managedNatures = previousPersistentModel.isPresent() ? Sets.newLinkedHashSet(previousPersistentModel.getManagedNatures()) : Sets.<String>newLinkedHashSet();

        Set<String> modelNatures = toNatures(projectNatures);
        IProjectDescription description = project.getDescription();
        Set<String> existingNatures = Sets.newLinkedHashSet(Arrays.asList(description.getNatureIds()));

        Result<String> result = ManagedModelMergingStrategy.calculate(existingNatures, modelNatures, managedNatures);

        description.setNatureIds(result.getNextElements().toArray(new String[0]));
        project.setDescription(description, monitor);
        persistentModel.managedNatures(result.getNextManaged());
    }

    private static Set<String> toNatures(Optional<List<OmniEclipseProjectNature>> projectNatures) {
        Set<String> natures = Sets.newLinkedHashSet();
        if (projectNatures.isPresent()) {
            natures.addAll(toNatures(projectNatures.get()));
        }
        natures.add(GradleProjectNature.ID);
        return natures;
    }

    private static Set<? extends String> toNatures(List<OmniEclipseProjectNature> projectNatures) {
        Set<String> natures = Sets.newLinkedHashSet();
        for (OmniEclipseProjectNature projectNature : projectNatures) {
            String id = projectNature.getId();
            if (CorePlugin.workspaceOperations().isNatureRecognizedByEclipse(id)) {
                natures.add(id);
            }
        }
        return natures;
    }
}
