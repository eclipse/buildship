/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.testprogress;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.testprogress.internal.DefaultGradleTestRunSession;

/**
 * Factory to create {@link GradleTestRunSession} instances.
 */
public final class GradleTestRunSessionFactory {

    private GradleTestRunSessionFactory() {
    }

    /**
     * Creates a new test session and associates it with the given Eclipse Java project.
     *
     * @param launch the launch instance as part of which the tests are executed in Gradle
     * @param project the associated Java project, can be null
     * @return the new instance
     */
    public static GradleTestRunSession newSession(ILaunch launch, IJavaProject project) {
        return new DefaultGradleTestRunSession(launch, project);
    }

}
