/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.wizard.project;

import java.io.File;
import java.util.List;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ProgressListener;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.operation.BaseToolingApiOperation;
import org.eclipse.buildship.core.util.gradle.TransientRequestAttributes;
import org.eclipse.buildship.core.util.progress.CancellationForwardingListener;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.workspace.GradleBuild;

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
                GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfig);
                TransientRequestAttributes transientAttributes = getTransientRequestAttributes(tokenSource, monitor);
                RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(buildConfig);
                gradleBuild.newBuildLauncher(runConfiguration, CharStreams.nullWriter(), transientAttributes).forTasks(tasks.toArray(new String[tasks.size()])).run();
            }
        }
    }

    private static TransientRequestAttributes getTransientRequestAttributes(CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        ProgressListener delegatingListener = DelegatingProgressListener.withFullOutput(monitor);
        CancellationForwardingListener cancellationListener = new CancellationForwardingListener(monitor, tokenSource);
        ImmutableList<ProgressListener> progressListeners = ImmutableList.of(delegatingListener, cancellationListener);
        ImmutableList<org.gradle.tooling.events.ProgressListener> eventListeners = ImmutableList.<org.gradle.tooling.events.ProgressListener> of();
        return new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), streams.getInput(), progressListeners, eventListeners, tokenSource.token());
    }
}
