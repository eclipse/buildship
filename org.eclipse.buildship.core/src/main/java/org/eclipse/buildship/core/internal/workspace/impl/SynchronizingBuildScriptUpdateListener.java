/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.util.Set;

import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.internal.operation.ToolingApiJobResultHandler;
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.workspace.GradleBuild;
import org.eclipse.buildship.core.internal.workspace.SynchronizationJob;

/**
 * Executes project synchronization if the corresponding preference is enabled and the user changes
 * the build script.
 *
 * @author Donat Csikos
 */
public final class SynchronizingBuildScriptUpdateListener implements IResourceChangeListener {

    private SynchronizingBuildScriptUpdateListener() {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {

        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                visitDelta(delta);
            } catch (CoreException e) {
                CorePlugin.logger().warn("Failed to detect project changes", e);
            }
        }
    }

    private boolean isEnabledInPreferences(IProject project) {
        ProjectConfiguration configuration = CorePlugin.configurationManager().loadProjectConfiguration(project);
        return configuration.getBuildConfiguration().isAutoSync();
    }

    private void visitDelta(IResourceDelta delta) throws CoreException {
        delta.accept(new IResourceDeltaVisitor() {

            @Override
            public boolean visit(IResourceDelta delta) throws CoreException {
                try {
                    return doVisitDelta(delta);
                } catch (Exception e) {
                    throw new CoreException(new Status(IStatus.WARNING, CorePlugin.PLUGIN_ID, "SynchronizingBuildScriptUpdateListener failed", e));
                }
            }
        });
    }

    private boolean doVisitDelta(IResourceDelta delta) throws Exception {
        IResource resource = delta.getResource();
        if (resource instanceof IProject) {
            IProject project = (IProject) resource;
            if (GradleProjectNature.isPresentOn(project) && isEnabledInPreferences(project)) {
                executeSyncIfBuildScriptChanged(project, delta);
            }
            return false;
        } else {
            return resource instanceof IWorkspaceRoot;
        }
    }

    private void executeSyncIfBuildScriptChanged(final IProject project, IResourceDelta delta) {
        if (hasBuildScriptFileChanged(project, delta.getAffectedChildren())) {
            GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(project).get();
            SynchronizationJob job = new SynchronizationJob(gradleBuild);
            job.setResultHandler(new ResultHander());
            job.schedule();
        }
    }

    private boolean hasBuildScriptFileChanged(IProject project, IResourceDelta[] deltas) {
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project);
        if (!model.isPresent())  {
            return false;
        } else {
            IPath buildScriptPath = model.getbuildScriptPath();
            Set<IPath> affectedResourcePaths  = collectAffectedResourcePaths(deltas);
            return affectedResourcePaths.contains(buildScriptPath);
        }
    }

    private Set<IPath> collectAffectedResourcePaths(IResourceDelta[] children) {
        Set<IPath> result = Sets.newHashSet();
        collectAffectedResourcePaths(result, children);
        return result;
    }

    private void  collectAffectedResourcePaths(Set<IPath> result, IResourceDelta[] deltas) {
        for (IResourceDelta delta : deltas) {
            result.add(delta.getResource().getProjectRelativePath());
            collectAffectedResourcePaths(result, delta.getAffectedChildren());
        }
    }

    public static SynchronizingBuildScriptUpdateListener createAndRegister() {
        SynchronizingBuildScriptUpdateListener listener = new SynchronizingBuildScriptUpdateListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
        return listener;
    }

    public void close() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    /**
     * Custom result handler that only logs the failure.
     */
    private static final class ResultHander implements ToolingApiJobResultHandler<Void> {

        @Override
        public void onSuccess(Void result) {
        }

        @Override
        public void onFailure(ToolingApiStatus status) {
            CorePlugin.getInstance().getLog().log(status);
        }
    }
}
