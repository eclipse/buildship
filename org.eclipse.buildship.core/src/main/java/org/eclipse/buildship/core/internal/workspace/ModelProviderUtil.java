/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Collection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.util.gradle.IdeFriendlyClassLoading;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.SimpleIntermediateResultHandler;

public class ModelProviderUtil {

    public static Collection<EclipseProject> fetchAllEclipseProjectsWithSyncTask(InternalGradleBuild build, CancellationTokenSource tokenSource, FetchStrategy fetchStrategy, IProgressMonitor monitor) throws Exception {
        return build.withConnection(connection -> {
            BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
            GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());

            if (supportsSyncTasksInEclipsePluginConfig(gradleVersion)) {
                return runTasksAndQueryCompositeEclipseModel(connection);
            } else if (supportsCompositeBuilds(gradleVersion)) {
                return queryCompositeEclipseModel(connection);
            } else {
                return queryEclipseModel(connection);
            }
        }, monitor);

    }

    private static boolean supportsSyncTasksInEclipsePluginConfig(GradleVersion gradleVersion) {
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("5.4")) >= 0;
    }

    private static boolean supportsCompositeBuilds(GradleVersion gradleVersion) {
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("3.3")) >= 0;
    }

    private static Collection<EclipseProject> runTasksAndQueryCompositeEclipseModel(ProjectConnection connection) {
        BuildAction<Collection<EclipseProject>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class);
        SimpleIntermediateResultHandler<Collection<EclipseProject>> resultHandler = new SimpleIntermediateResultHandler<>();
        connection.action().projectsLoaded(IdeFriendlyClassLoading.loadClass(TellGradleToRunSynchronizationTasks.class), new SimpleIntermediateResultHandler<Void>()).buildFinished(query, resultHandler).build()
                .forTasks().run();
        return resultHandler.getValue();
    }

    private static Collection<EclipseProject> queryCompositeEclipseModel(ProjectConnection connection) {
        BuildAction<Collection<EclipseProject>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class);
        return connection.action(query).run();
    }

    private static Collection<EclipseProject> queryEclipseModel(ProjectConnection connection) {
        return ImmutableList.of(connection.getModel(EclipseProject.class));
    }
}
