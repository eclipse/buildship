/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.build.BuildEnvironment;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.buildship.core.FixedVersionGradleDistribution;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.WrapperGradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

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
        SubMonitor progress = SubMonitor.convert(monitor, 2);

        File projectDir = buildConfig.getRootProjectDirectory().getAbsoluteFile();
        if (!projectDir.exists() && projectDir.mkdir()) {
            GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig);
            String[] tasks = initTaskFor(gradleBuild, progress.newChild(1));
            RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(buildConfig);
            GradleProgressAttributes progressAttributes = GradleProgressAttributes.builder(tokenSource, progress.newChild(1))
                    .forBackgroundProcess()
                    .withFilteredProgress()
                    .build();
            gradleBuild.newBuildLauncher(runConfiguration, progressAttributes).forTasks(tasks).run();
        } else {
            progress.worked(2);
        }
    }

    private static String[] initTaskFor(GradleBuild gradleBuild, IProgressMonitor monitor) {
        BuildConfiguration buildConfig = gradleBuild.getBuildConfig();
        GradleDistribution distribution = buildConfig.getGradleDistribution();
        GradleVersion gradleVersion;
        if (distribution instanceof FixedVersionGradleDistribution) {
            gradleVersion = GradleVersion.version(((FixedVersionGradleDistribution)distribution).getVersion());
        } else if (distribution instanceof WrapperGradleDistribution) {
            gradleVersion = GradleVersion.current();
        } else {
            BuildEnvironment buildEnvironment = gradleBuild.getModelProvider().fetchModel(BuildEnvironment.class, FetchStrategy.FORCE_RELOAD, GradleConnector.newCancellationTokenSource(), monitor);
            gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        }

        if (gradleVersion.compareTo(GradleVersion.version("5.0-rc-1")) < 0) {
            return new String[]{ "init", "--type", "java-library" };
        } else {
            return new String[]{ "init", "--type", "java-library", "--dsl", "groovy", "--test-framework", "junit", "--project-name", buildConfig.getRootProjectDirectory().getName(), "--package", "org.example"};
        }
    }

    @Override
    public ISchedulingRule getRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
