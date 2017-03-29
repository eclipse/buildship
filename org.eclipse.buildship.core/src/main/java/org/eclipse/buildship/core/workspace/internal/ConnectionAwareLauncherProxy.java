/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Creates long-running TAPI operations that closes their project connection after the execution is
 * finished.
 *
 * @author Donat Csikos
 */
final class ConnectionAwareLauncherProxy implements InvocationHandler {

    private final LongRunningOperation launcher;
    private final ProjectConnection connection;

    private ConnectionAwareLauncherProxy(ProjectConnection connection, LongRunningOperation target) {
        this.connection = connection;
        this.launcher = target;
    }

    @SuppressWarnings("unchecked")
    static <T> ModelBuilder<T> newModelBuilder(ProjectConnection connection, Class<T> model) {
        ModelBuilder<T> builder = connection.model(model);
        return (ModelBuilder<T>) newProxyInstance(connection, builder);
    }

    @SuppressWarnings("unchecked")
    static <T> BuildActionExecuter<Collection<T>> newCompositeModelQueryExecuter(ProjectConnection connection, Class<T> model) {
        BuildActionExecuter<Collection<T>> executer = connection.action(compositeModelQuery(model));
        return (BuildActionExecuter<Collection<T>>) newProxyInstance(connection, executer);
    }

    private static <T> BuildAction<Collection<T>> compositeModelQuery(Class<T> model) {
        if (Platform.inDevelopmentMode()) {
            return ideFriendlyCompositeModelQuery(model);
        } else {
            return new CompositeModelQuery<>(model);
        }
    }

    @SuppressWarnings({ "resource", "unchecked" })
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
            URLClassLoader actionClassLoader = new URLClassLoader(new URL[] { actionRootUrl }, tapiClassloader);
            Class<?> actionClass = actionClassLoader.loadClass(CompositeModelQuery.class.getName());
            return (BuildAction<Collection<T>>) actionClass.getConstructor(Class.class).newInstance(model);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    static BuildLauncher newBuildLauncher(ProjectConnection connection) {
        BuildLauncher launcher = connection.newBuild();
        return (BuildLauncher) newProxyInstance(connection, launcher);
    }

    static TestLauncher newTestLauncher(ProjectConnection connection) {
        TestLauncher launcher = connection.newTestLauncher();
        return (TestLauncher) newProxyInstance(connection, launcher);
    }

    private static Object newProxyInstance(ProjectConnection connection, LongRunningOperation launcher) {
        return Proxy.newProxyInstance(launcher.getClass().getClassLoader(),
                                      launcher.getClass().getInterfaces(),
                                      new ConnectionAwareLauncherProxy(connection, launcher));
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        // BuildLauncher and TestLauncher have the same method signature for execution:
        // #run() and #run(ResultHandler)
        if (m.getName().equals("run") || m.getName().equals("get")) {
            if (args == null) {
                return invokeRun(m);
            } else if (args.length == 1 && args[0].getClass() == ResultHandler.class) {
                return invokeRun(m, args[0]);
            }
        }
        return invokeOther(m, args);
    }

    private Object invokeRun(Method m) throws Throwable {
        try {
            return m.invoke(this.launcher);
        } finally {
            this.connection.close();
        }
    }

    private Object invokeRun(Method m, Object resultHandler) throws Throwable {
        @SuppressWarnings("unchecked")
        final ResultHandler<Object> handler = (ResultHandler<Object>) resultHandler;
        return m.invoke(this.launcher, new ResultHandler<Object>() {

            @Override
            public void onComplete(Object result) {
                try {
                    handler.onComplete(result);
                } finally {
                    ConnectionAwareLauncherProxy.this.connection.close();
                }
            }

            @Override
            public void onFailure(GradleConnectionException e) {
                try {
                    handler.onFailure(e);
                } finally {
                    ConnectionAwareLauncherProxy.this.connection.close();
                }
            }
        });
    }

    private Object invokeOther(Method m, Object[] args) throws Throwable {
        return m.invoke(this.launcher, args);
    }
}
