/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace.impl;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.model.build.BuildEnvironment;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.GradleArguments;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;

/**
 * Provides long-running TAPI operation instances that close their project connection after the execution is
 * finished.
 *
 * @author Donat Csikos
 */
@SuppressWarnings("unchecked")
final class ConnectionAwareLauncherProxy implements InvocationHandler {

    private final LongRunningOperation launcher;
    private final ProjectConnection connection;
    private static URLClassLoader ideFriendlyCustomActionClassLoader;

    private ConnectionAwareLauncherProxy(ProjectConnection connection, LongRunningOperation target) {
        this.connection = connection;
        this.launcher = target;
    }

    static <T> ModelBuilder<T> newModelBuilder(Class<T> model, GradleArguments gradleArguments, GradleProgressAttributes progressAttributes) {
        ProjectConnection connection = openConnection(gradleArguments);
        ModelBuilder<T> builder = connection.model(model);
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        applyConfiguration(builder, gradleArguments, buildEnvironment, progressAttributes);
        return (ModelBuilder<T>) newProxyInstance(connection, builder);
    }

    static <T> BuildActionExecuter<Collection<T>> newCompositeModelQueryExecuter(Class<T> model, GradleArguments gradleArguments, GradleProgressAttributes progressAttributes) {
        ProjectConnection connection = openConnection(gradleArguments);
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        BuildActionExecuter<Collection<T>> executer = connection.action(compositeModelQuery(model));
        applyConfiguration(executer, gradleArguments, buildEnvironment, progressAttributes);
        return (BuildActionExecuter<Collection<T>>) newProxyInstance(connection, executer);
    }

    static BuildLauncher newBuildLauncher(GradleArguments gradleArguments, GradleProgressAttributes progressAttributes) {
        ProjectConnection connection = openConnection(gradleArguments);
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        BuildLauncher launcher = connection.newBuild();
        describeAndApplyConfiguration(launcher, gradleArguments, buildEnvironment, progressAttributes);
        return (BuildLauncher) newProxyInstance(connection, launcher);
    }

    static TestLauncher newTestLauncher(GradleArguments gradleArguments, GradleProgressAttributes progressAttributes) {
        ProjectConnection connection = openConnection(gradleArguments);
        BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
        TestLauncher launcher = connection.newTestLauncher();
        describeAndApplyConfiguration(launcher, gradleArguments, buildEnvironment, progressAttributes);
        return (TestLauncher) newProxyInstance(connection, launcher);
    }

    private static ProjectConnection openConnection(GradleArguments gradleArguments) {
        GradleConnector connector = GradleConnector.newConnector();
        gradleArguments.applyTo(connector);
        return connector.connect();
    }

    private static void applyConfiguration(LongRunningOperation operation, GradleArguments gradleArguments, BuildEnvironment buildEnvironment,
            GradleProgressAttributes attributes) {
        gradleArguments.applyTo(operation, buildEnvironment);
        attributes.applyTo(operation);
    }

    private static void describeAndApplyConfiguration(LongRunningOperation operation, GradleArguments gradleArguments, BuildEnvironment buildEnvironment,
            GradleProgressAttributes progressAttributes) {
        gradleArguments.applyTo(operation, buildEnvironment);
        gradleArguments.describe(progressAttributes, buildEnvironment);
        progressAttributes.applyTo(operation);
    }

    private static <T> BuildAction<Collection<T>> compositeModelQuery(Class<T> model) {
        if (Platform.inDevelopmentMode()) {
            return ideFriendlyCompositeModelQuery(model);
        } else {
            return new CompositeModelQuery<>(model);
        }
    }

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
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        } finally {
            closeConnection();
        }
    }

    private Object invokeRun(Method m, Object resultHandler) throws Throwable {
        final ResultHandler<Object> handler = (ResultHandler<Object>) resultHandler;
        return m.invoke(this.launcher, new ResultHandler<Object>() {

            @Override
            public void onComplete(Object result) {
                try {
                    handler.onComplete(result);
                } finally {
                    closeConnection();
                }
            }

            @Override
            public void onFailure(GradleConnectionException e) {
                try {
                    handler.onFailure(e);
                } finally {
                    closeConnection();
                }
            }
        });
    }

    private void closeConnection() {
        this.connection.close();
        if (ideFriendlyCustomActionClassLoader != null) {
            try {
                ideFriendlyCustomActionClassLoader.close();
            } catch (IOException e) {
                CorePlugin.logger().error("Can't close URL class loader", e);
            }
        }
    }

    private Object invokeOther(Method m, Object[] args) throws Throwable {
        return m.invoke(this.launcher, args);
    }
}
