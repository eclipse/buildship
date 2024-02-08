/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.util.Map;
import java.util.Optional;

import org.gradle.tooling.IntermediateResultHandler;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.util.gradle.IdeFriendlyClassLoading;
import org.eclipse.buildship.core.internal.workspace.TellGradleToRunAutoSyncTasks;

/**
 * Backing implementation class for the {@link org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder}.
 * <p/>
 * Wired to the project by the {@link org.eclipse.buildship.core.internal.configuration.GradleProjectBuilder}.
 * <p/>
 * Defined as an extension point of <code>org.eclipse.core.resources.builders</code> in the <i>plugin.xml</i>.
 */
public final class DefaultGradleProjectBuilder extends IncrementalProjectBuilder {

    // In Eclipse 3.6, this method has no generics in the argument list (Map<String,String>)
    @Override
    protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args, IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        if (kind == AUTO_BUILD) {
            runAutoBuild(monitor, project);
        }

        if (kind == FULL_BUILD) {
            fullBuild(project);
        } else {
            IResourceDelta delta = getDelta(project);
            if (delta == null) {
                fullBuild(project);
            } else {
                incrementalBuild(delta, project);
            }
        }
        return null;
    }

    private void fullBuild(IProject project) throws CoreException {
        // validate project
        new GradleProjectValidationResourceDeltaVisitor(project).validate();
    }

    private void incrementalBuild(IResourceDelta delta, IProject project) throws CoreException {
        // validate project
        delta.accept(new GradleProjectValidationResourceDeltaVisitor(project));
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        // delete markers
        GradleProjectMarker.INSTANCE.removeMarkerFromResourceRecursively(getProject());
    }

    private void runAutoBuild(IProgressMonitor monitor, IProject project) {
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project);
        if (model.isPresent()) {
            if (model.hasAutoBuildTasks()) {
                Optional<GradleBuild> gradleBuild = GradleCore.getWorkspace().getBuild(project);
                if (gradleBuild.isPresent()) {
                    try {
                        gradleBuild.get().withConnection(connection -> {
                            return connection.action()
                                .projectsLoaded(IdeFriendlyClassLoading.loadClass(TellGradleToRunAutoSyncTasks.class), NoOpResultHandler.newInstance())
                                .build()
                                .forTasks()
                                .run();
                        }, monitor);
                    } catch (Exception e) {
                        CorePlugin.logger().warn("Can't run auto-build tasks", e);
                    }
                }
            }
        }
    }

    private static final class NoOpResultHandler<T> implements IntermediateResultHandler<T> {

        @Override
        public void onComplete(T result) {
        }

        static <T> NoOpResultHandler<T> newInstance() {
            return new NoOpResultHandler<>();
        }

    }
}
