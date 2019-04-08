/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.io.IOException;
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

public class IdeFriendlyClassLoading {

    // When Buildship is launched from the IDE - as an Eclipse application or as a plug-in
    // test - the URLs returned by the Equinox class loader is incorrect. This means, the
    // Tooling API is unable to find the referenced build actions and fails with a CNF
    // exception. To work around that, we look up the build action class locations and load the
    // classes via an isolated URClassLoader.

    private static URLClassLoader ideFriendlyClassLoader;

    private IdeFriendlyClassLoading() {
    }

    public static <T> BuildAction<Collection<T>> loadCompositeModelQuery(Class<T> model) {
        try {
            if (Platform.inDevelopmentMode()) {
                Class<?> actionClass = getIdeFriendlyClassLoader().loadClass(CompositeModelQuery.class.getName());
                @SuppressWarnings("unchecked")
                BuildAction<Collection<T>> instance = (BuildAction<Collection<T>>) actionClass.getConstructor(Class.class).newInstance(model);
                return instance;
            } else {
                return new CompositeModelQuery<>(model);
            }
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    public static <T> T loadClass(Class<T> cls) {
        try {
            if (Platform.inDevelopmentMode()) {
                @SuppressWarnings("unchecked")
                T instance = (T) getIdeFriendlyClassLoader().loadClass(cls.getName()).newInstance();
                return instance;
            } else {
                return cls.newInstance();
            }
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private static final ClassLoader getIdeFriendlyClassLoader() throws IOException {
        ClassLoader coreClassloader = DefaultModelProvider.class.getClassLoader();
        ClassLoader tapiClassloader = ProjectConnection.class.getClassLoader();
        URL actionRootUrl = FileLocator.resolve(coreClassloader.getResource(""));
        if (ideFriendlyClassLoader == null) {
            ideFriendlyClassLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
        }
        return ideFriendlyClassLoader;
    }


}
