/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.workspace.CompositeModelQuery;
import org.eclipse.buildship.core.internal.workspace.DefaultModelProvider;

public class BuildActionUtil {

    private static URLClassLoader ideFriendlyCustomActionClassLoader;

    private BuildActionUtil() {
    }

    public static <T> BuildAction<Collection<T>> compositeModelQuery(Class<T> model) {
        if (Platform.inDevelopmentMode()) {
            return ideFriendlyCompositeModelQuery(model);
        } else {
            return new CompositeModelQuery<>(model);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> BuildAction<Collection<T>> ideFriendlyCompositeModelQuery(Class<T> model) {
        // When Buildship is launched from the IDE - as an Eclipse application or as a plug-in
        // test - the URLs returned by the Equinox class loader is incorrect. This means, the
        // Tooling API is unable to find the referenced build actions and fails with a CNF
        // exception. To work around that, we look up the build action class locations and load the
        // classes via an isolated URClassLoader.
        try {
            ClassLoader coreClassloader = DefaultModelProvider.class.getClassLoader();
            ClassLoader tapiClassloader = ProjectConnection.class.getClassLoader();
            URL actionRootUrl = FileLocator.resolve(coreClassloader.getResource(""));
            if (ideFriendlyCustomActionClassLoader == null) {
                ideFriendlyCustomActionClassLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
            }
            Class<?> actionClass = ideFriendlyCustomActionClassLoader.loadClass(CompositeModelQuery.class.getName());
            return (BuildAction<Collection<T>>) actionClass.getConstructor(Class.class).newInstance(model);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}
