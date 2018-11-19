/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils;

public class EclipseProjectQuery implements Function<ProjectConnection, Map<File, EclipseProject>> {

    public static final String BUILD_ACTION_ID = "org.eclipse.buildship.configurators.eclipseProjectQuery";

    private static URLClassLoader ideFriendlyCustomActionClassLoader;

    @Override
    public Map<File, EclipseProject> apply(ProjectConnection pc) {
        Collection<EclipseProject> roots = injectCompatibilityModel(queryModel(pc));
        return collectAllProjects(roots);
    }

    private Collection<EclipseProject> queryModel(ProjectConnection pc) {
        if (supportsCompositeBuilds(pc)) {
            return pc.action(compositeModelQuery(EclipseProject.class)).run();
        } else {
            return ImmutableList.of(pc.getModel(EclipseProject.class));
        }
    }

    private static <T> BuildAction<Collection<T>> compositeModelQuery(Class<T> model) {
        if (Platform.inDevelopmentMode()) {
            return ideFriendlyCompositeModelQuery(model);
        } else {
            return new CompositeModelQuery<>(model);
        }
    }

    // TODO (donat) remove the duplicates from compositemodelbuilder

    @SuppressWarnings("unchecked")
    private static <T> BuildAction<Collection<T>> ideFriendlyCompositeModelQuery(Class<T> model) {
        // When Buildship is launched from the IDE - as an Eclipse application or as a plugin-in
        // test - the URLs returned by the Equinox class loader is incorrect. This means, the
        // Tooling API is unable to find the referenced build actions and fails with a CNF
        // exception. To work around that, we look up the build action class locations and load the
        // classes via an isolated URClassLoader.
        try {
            ClassLoader coreClassloader = ConnectionAwareLauncherProxy.class.getClassLoader();
            ClassLoader tapiClassloader = ProjectConnection.class.getClassLoader();
            URL actionRootUrl = FileLocator.resolve(coreClassloader.getResource(""));
            ideFriendlyCustomActionClassLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
            Class<?> actionClass = ideFriendlyCustomActionClassLoader.loadClass(CompositeModelQuery.class.getName());
            return (BuildAction<Collection<T>>) actionClass.getConstructor(Class.class).newInstance(model);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private static boolean supportsCompositeBuilds(ProjectConnection pc) {
        BuildEnvironment buildEnvironment = pc.getModel(BuildEnvironment.class);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version("3.3")) >= 0;
    }

    private static Collection<EclipseProject> injectCompatibilityModel(Collection<EclipseProject> models) {
        ImmutableList.Builder<EclipseProject> result = ImmutableList.builder();
        for (EclipseProject model : models) {
            result.add(ModelUtils.createCompatibilityModel(model));
        }
        return result.build();
    }

    private static Map<File, EclipseProject> collectAllProjects(Collection<EclipseProject> roots) {
        ImmutableMap.Builder<File, EclipseProject> result = ImmutableMap.builder();
        for (EclipseProject root : roots) {
            for (EclipseProject p : HierarchicalElementUtils.getAll(root)) {
                result.put(p.getProjectDirectory(), p);
            }
        }
        return result.build();
    }
}
