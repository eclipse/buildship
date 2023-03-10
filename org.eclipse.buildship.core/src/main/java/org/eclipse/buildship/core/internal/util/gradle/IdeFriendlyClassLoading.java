/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.workspace.CompositeModelQuery;
import org.eclipse.buildship.core.internal.workspace.TellGradleToRunAutoSyncTasks;

/**
 * When Buildship is launched from the IDE - as an Eclipse application or as a plug-in
 * test - the URLs returned by the Equinox class loader is incorrect. This means, the
 * Tooling API is unable to find the referenced build actions and fails with a CNF
 * exception. To work around that, we look up the build action class locations and load the
 * classes via an isolated URClassLoader.
 */
public class IdeFriendlyClassLoading {

    private static URLClassLoader classLoader;

    private IdeFriendlyClassLoading() {
    }

    @SuppressWarnings("unchecked")
    public static <T, U> BuildAction<Map<String, T>>loadCompositeModelQuery(Class<T> model, Class<U> parameterType, Action<? super U> parameter) {
        return (BuildAction<Map<String, T>>) loadClass(CompositeModelQuery.class, new Object[] { model, parameterType, parameter });
    }

    @SuppressWarnings("unchecked")
    public static <T> BuildAction<Map<String, T>> loadCompositeModelQuery(Class<T> model) {
        return (BuildAction<Map<String, T>>) loadClass(CompositeModelQuery.class, model );
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadClass(Class<T> cls) {
        try {
            if (Platform.inDevelopmentMode()) {
                return (T) loadClassWithIdeFriendlyClassLoader(cls.getName()).newInstance();
            } else {
                return cls.newInstance();
            }
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadClass(Class<T> cls, Object... arguments) {
        try {
            Class<T> theClass;
            if (Platform.inDevelopmentMode()) {
                theClass = (Class<T>) loadClassWithIdeFriendlyClassLoader(cls.getName());
            } else {
                theClass = cls;
            }
            Class<?>[] parameterTypes = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                parameterTypes[i] = arguments[i].getClass();
            }

            Constructor<T> constructor = findConstructor(theClass, arguments);
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> findConstructor(Class<T> theClass, Object[] arguments) throws NoSuchMethodException {
        for (Constructor<?> c : theClass.getConstructors()) {
            if (c.getParameterCount() != arguments.length) {
                continue;
            }
            Parameter[] parameters = c.getParameters();
            boolean foundConstructor = true;
            for (int i = 0; i < parameters.length; i++) {
                if (!parameters[i].getType().isInstance(arguments[i])) {
                    foundConstructor = false;
                    break;
                }
            }
            if (foundConstructor) {
                return (Constructor<T>)c;
            }
        }
        throw new NoSuchMethodException("Failed fo find constructor on " + theClass.getName() + " accepting " + Arrays.asList(arguments));
    }

    /**
     * Closes IDE-friendly class loader.
     * <p>
     * This method does not do anything if Eclipse is not in development mode.
     */
    public static void cleanup() {
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                throw new GradlePluginsRuntimeException(e);
            }
            classLoader = null;
        }
    }

    private static final Class<?> loadClassWithIdeFriendlyClassLoader(String classname) throws Exception {
        ClassLoader compatClassloader = TellGradleToRunAutoSyncTasks.class.getClassLoader();
        ClassLoader tapiClassloader = ProjectConnection.class.getClassLoader();
        URL actionRootUrl = FileLocator.resolve(compatClassloader.getResource(""));

        if (classLoader == null) {
            classLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
        }
        return classLoader.loadClass(classname);
    }
}
