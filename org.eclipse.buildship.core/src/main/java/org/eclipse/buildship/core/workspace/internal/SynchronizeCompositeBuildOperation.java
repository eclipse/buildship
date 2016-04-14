/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473348
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes the given Gradle composite build with the Eclipse workspace using
 * {@link SynchronizeGradleBuildOperation}.
 *
 * <p/>
 * This operation changes resources. It will acquire the workspace scheduling rule to ensure an
 * atomic operation.
 *
 */
final class SynchronizeCompositeBuildOperation implements IWorkspaceRunnable {

    private final OmniEclipseWorkspace workspaceModel;
    private final Set<FixedRequestAttributes> builds;
    private final NewProjectHandler newProjectHandler;

    SynchronizeCompositeBuildOperation(OmniEclipseWorkspace workspaceModel, Set<FixedRequestAttributes> builds, NewProjectHandler newProjectHandler) {
        this.workspaceModel = workspaceModel;
        this.builds = builds;
        this.newProjectHandler = newProjectHandler;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        JavaCore.run(new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                synchronizeGradleBuilds(monitor);
            }
        }, monitor);
    }

    private void synchronizeGradleBuilds(IProgressMonitor monitor) throws CoreException {
        SubMonitor progress = SubMonitor.convert(monitor, this.builds.size());
        for (FixedRequestAttributes build : this.builds) {
            new SynchronizeGradleBuildOperation(this.workspaceModel, build, this.newProjectHandler).run(progress.newChild(1));
        }
    };

}
