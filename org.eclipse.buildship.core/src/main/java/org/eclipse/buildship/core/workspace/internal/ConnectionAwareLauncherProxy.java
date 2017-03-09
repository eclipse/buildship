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

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;

/**
 * Creates build and test launchers that closes their project connection after the execution is
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
        if (m.getName().equals("run")) {
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
