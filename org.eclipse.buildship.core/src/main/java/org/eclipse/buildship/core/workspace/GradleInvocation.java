/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace;

/**
 * Common interface to launch Gradle builds and tests.
 *
 * @author Donat Csikos
 *
 */
public interface GradleInvocation {

    /**
     * Executes a Gradle build. Opens a new tooling API connection, executes the build and and
     * closes the connection.
     */
    void executeAndWait();
}
