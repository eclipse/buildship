/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.gradle;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Provides a static runGradleBuild method, which runs a Gradle build with the given parameters.
 * This method should only be run within a ToolingApiWorkspaceJob.
 *
 */
public final class BuildRunner {

    private BuildRunner() {
    }

    public static void runGradleBuild(IProgressMonitor monitor, List<String> tasks, File workingDir, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome,
            Collection<String> jvmArguments, Collection<String> arguments) {
        // start tracking progress
        monitor.setTaskName(String.format("Launch Gradle tasks %s", tasks));

        // configure the request
        BuildLaunchRequest request = CorePlugin.toolingClient().newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));
        request.projectDir(workingDir);
        request.gradleDistribution(gradleDistribution);
        request.gradleUserHomeDir(gradleUserHome);
        request.javaHomeDir(javaHome);
        request.jvmArguments(jvmArguments.toArray(new String[jvmArguments.size()]));
        request.arguments(arguments.toArray(new String[arguments.size()]));

        // launch the build
        request.executeAndWait();
    }

}
