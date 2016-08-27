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

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Updates the natures on the target project.
 */
final class ProjectNatureUpdater {

    public static void update(IProject project, Optional<List<OmniEclipseProjectNature>> projectNatures, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        Set<String> natures = toNatures(projectNatures, description);
        description.setNatureIds(natures.toArray(new String[0]));
        project.setDescription(description, monitor);
    }

    private static Set<String> toNatures(Optional<List<OmniEclipseProjectNature>> projectNatures, IProjectDescription description) {
        Set<String> natures = Sets.newLinkedHashSet();
        if (projectNatures.isPresent()) {
            natures.addAll(toNatures(projectNatures.get()));
        } else {
            natures.addAll(Arrays.asList(description.getNatureIds()));
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
