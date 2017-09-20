/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.StandardClasspathProvider;

/**
 * Classpath provider for Gradle projects.
 * TODO (donat) implement this
 *
 * @author Donat Csikos
 */
public final class GradleClasspathProvider extends StandardClasspathProvider implements IRuntimeClasspathProvider {

    public static final String ID = "org.eclipse.buildship.core.classpathprovider";

    public GradleClasspathProvider() {
        super();
    }

    @Override
    public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
        System.err.println("computeUnresolvedClasspath called");
        return super.computeUnresolvedClasspath(configuration);
    }

    @Override
    public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
        System.err.println("resolveClasspath called");
        return super.resolveClasspath(entries, configuration);
    }
}
