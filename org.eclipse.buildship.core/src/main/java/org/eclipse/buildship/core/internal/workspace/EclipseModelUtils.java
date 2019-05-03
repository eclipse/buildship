/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.eclipse.buildship.core.internal.workspace;

import java.util.Collection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseRuntime;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.IdeFriendlyClassLoading;
import org.eclipse.buildship.core.internal.util.gradle.SimpleIntermediateResultHandler;

public final class EclipseModelUtils {

    private EclipseModelUtils() {
    }

    public static Collection<EclipseProject> queryModels(ProjectConnection connection) {
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        if (supportsSendingReservedProjects(gradleVersion)) {
            return queryCompositeModelWithRuntimInfo(connection, gradleVersion);
        } else if (supportsCompositeBuilds(gradleVersion)) {
            return queryCompositeModel(EclipseProject.class, connection);
        } else {
            return ImmutableList.of(queryModel(EclipseProject.class, connection));
        }
    }

    public static Collection<EclipseProject> runTasksAndQueryModels(ProjectConnection connection) {
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        if (supportsSendingReservedProjects(gradleVersion)) {
            return runTasksAndQueryCompositeModelWithRuntimInfo(connection, gradleVersion);
        } else if (supportsSyncTasksInEclipsePluginConfig(gradleVersion)) {
            return runTasksAndQueryCompositeModel(connection, gradleVersion);
        } else if (supportsCompositeBuilds(gradleVersion)) {
            return queryCompositeModel(EclipseProject.class, connection);
        } else {
            return ImmutableList.of(queryModel(EclipseProject.class, connection));
        }
    }

    private static boolean supportsSendingReservedProjects(GradleVersion gradleVersion) {
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("5.5")) >= 0;
    }

    private static boolean supportsSyncTasksInEclipsePluginConfig(GradleVersion gradleVersion) {
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("5.4")) >= 0;
    }

    private static boolean supportsCompositeBuilds(GradleVersion gradleVersion) {
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("3.3")) >= 0;
    }

    private static Collection<EclipseProject> runTasksAndQueryCompositeModelWithRuntimInfo(ProjectConnection connection, GradleVersion gradleVersion) {
        return runTasksAndQueryCompositeModel(connection, gradleVersion, IdeFriendlyClassLoading
                .loadCompositeModelQuery(EclipseProject.class, EclipseRuntime.class, new EclipseRuntimeConfigurer()));
    }

    private static Collection<EclipseProject> runTasksAndQueryCompositeModel(ProjectConnection connection, GradleVersion gradleVersion) {
        return runTasksAndQueryCompositeModel(connection, gradleVersion, IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class));
    }

    private static Collection<EclipseProject> runTasksAndQueryCompositeModel(ProjectConnection connection, GradleVersion gradleVersion,
            BuildAction<Collection<EclipseProject>> query) {
        SimpleIntermediateResultHandler<Collection<EclipseProject>> resultHandler = new SimpleIntermediateResultHandler<>();
        BuildAction<Void> projectsLoadedAction = IdeFriendlyClassLoading.loadClass(TellGradleToRunSynchronizationTasks.class);
        connection.action().projectsLoaded(projectsLoadedAction, new SimpleIntermediateResultHandler<Void>()).buildFinished(query, resultHandler).build().forTasks().run();
        return resultHandler.getValue();
    }

    private static Collection<EclipseProject> queryCompositeModelWithRuntimInfo(ProjectConnection connection, GradleVersion gradleVersion) {
        BuildAction<Collection<EclipseProject>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(EclipseProject.class, EclipseRuntime.class, new EclipseRuntimeConfigurer());
        return connection.action(query).run();
    }

    private static <T> Collection<T> queryCompositeModel(Class<T> model, ProjectConnection connection) {
        BuildAction<Collection<T>> query = IdeFriendlyClassLoading.loadCompositeModelQuery(model);
        return connection.action(query).run();
    }

    private static <T> T queryModel(Class<T> model, ProjectConnection connection) {
        return connection.getModel(model);
    }
}
