/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.buildship.core.workspace.GradleBuild.TestLauncherConfig;

/**
 * Launches a Gradle test.
 *
 * @author Donat Csikos
 */
public class GradleTestInvocation extends BaseGradleInvocation<TestLauncher> {

    private TestLauncherConfig config;

    public GradleTestInvocation(FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes, TestLauncherConfig config) {
        super(fixedAttributes, transientAttributes);
        this.config = config;
    }

    @Override
    protected TestLauncher create(ProjectConnection connection) {
        TestLauncher launcher = connection.newTestLauncher();
        this.config.apply(launcher);
        return launcher;
    }

    @Override
    protected void launch(TestLauncher launcher) {
        launcher.run();
    }
}
