/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.ProjectConnection;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.buildship.core.workspace.GradleBuild.BuildLauncherConfig;

/**
 * Launches a Gradle build.
 *
 * @author Donat Csikos
 */
public class GradleBuildInvocation extends BaseGradleInvocation<BuildLauncher> {
    private BuildLauncherConfig config;

    public GradleBuildInvocation(FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes, BuildLauncherConfig config) {
        super(fixedAttributes, transientAttributes);
        this.config = config;
    }

    @Override
    protected BuildLauncher create(ProjectConnection connection) {
        BuildLauncher launcher = connection.newBuild();
        this.config.apply(launcher);
        return launcher;
    }

    @Override
    protected void launch(BuildLauncher launcher) {
        launcher.run();
    }
}
