/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.List;

import org.gradle.tooling.CancellationTokenSource;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.LaunchConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;

/**
 * Creates the {@code java-library} Gradle template project in the target directory.
 *
 * @author Donat Csikos
 */
public class InitializeNewProjectOperation extends BaseToolingApiOperation {

    private final BuildConfiguration buildConfiguration;

    public InitializeNewProjectOperation(BuildConfiguration buildConfiguration) {
        super("Initialize project " + buildConfiguration.getRootProjectDirectory().getName());
        this.buildConfiguration = buildConfiguration;
    }

    @Override
    public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        initProjectIfNotExists(this.buildConfiguration, tokenSource, monitor);

    }

    private static void initProjectIfNotExists(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        File projectDir = buildConfig.getRootProjectDirectory().getAbsoluteFile();
        if (!projectDir.exists()) {
            if (projectDir.mkdir()) {
                List<String> tasks = ImmutableList.of("init", "--type", "java-library");
                InternalGradleBuild gradleBuild = CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig);
                LaunchConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(buildConfig);
                GradleProgressAttributes progressAttributes = GradleProgressAttributes.builder(tokenSource, monitor)
                        .forNonInteractiveBackgroundProcess()
                        .withFilteredProgress()
                        .build();
                gradleBuild.newBuildLauncher(runConfiguration, progressAttributes).forTasks(tasks.toArray(new String[tasks.size()])).run();
            }
        }
    }

    @Override
    public ISchedulingRule getRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
