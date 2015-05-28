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

package org.eclipse.buildship.core.launch;

import java.io.File;
import java.util.List;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;

/**
 * Runs a Gradle task.
 */
public final class RunGradleTaskJob extends ToolingApiJob {

    private GradleRunConfigurationAttributes configurationAttributes;

    public RunGradleTaskJob(ProjectImportConfiguration configuration) {
        this(configuration.getGradleTask().getValue(), configuration.getProjectDir().getValue(), configuration.getGradleDistribution().getValue().toGradleDistribution(),
                configuration.getGradleUserHome().getValue(), configuration.getJavaHome().getValue(), configuration.getJvmArguments().getValue(), configuration.getArguments()
                        .getValue());
    }

    public RunGradleTaskJob(List<String> tasks, File workingDir, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, String jvmArguments, String arguments) {
        super("Launching Gradle tasks");
        File workingDirectory = Preconditions.checkNotNull(workingDir);
        this.configurationAttributes = GradleRunConfigurationAttributes.with(tasks, workingDirectory.getAbsolutePath(), gradleDistribution,
                gradleUserHome != null ? gradleUserHome.getAbsolutePath() : null, javaHome != null ? javaHome.getAbsolutePath() : null,
                CollectionsUtils.splitBySpace(jvmArguments), CollectionsUtils.splitBySpace(arguments),
                false, false);
    }

    public RunGradleTaskJob(ILaunchConfiguration launchConfiguration) {
        super("Launching Gradle tasks");
        this.configurationAttributes = GradleRunConfigurationAttributes.from(launchConfiguration);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            runLaunchConfiguration(monitor);
            return Status.OK_STATUS;
        } catch (BuildCancelledException e) {
            // if the job was cancelled by the user, do not show an error dialog
            CorePlugin.logger().info(e.getMessage());
            return Status.CANCEL_STATUS;
        } catch (BuildException e) {
            // return only a warning if there was a problem while running the Gradle build since the
            // error is also visible in the Gradle console
            return new Status(IStatus.WARNING, CorePlugin.PLUGIN_ID, "Gradle build failure during task execution.", e);
        } catch (Exception e) {
            return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Launching the Gradle tasks failed.", e);
        } finally {
            monitor.done();
        }
    }

    public void runLaunchConfiguration(IProgressMonitor monitor) {
        List<String> tasks = this.configurationAttributes.getTasks();
        File workingDir = this.configurationAttributes.getWorkingDir();
        GradleDistribution gradleDistribution = this.configurationAttributes.getGradleDistribution();
        File gradleUserHome = this.configurationAttributes.getGradleUserHome();
        File javaHome = this.configurationAttributes.getJavaHome();
        ImmutableList<String> jvmArguments = this.configurationAttributes.getJvmArguments();
        ImmutableList<String> arguments = this.configurationAttributes.getArguments();

        // start tracking progress
        monitor.beginTask(String.format("Launch Gradle tasks %s", tasks), IProgressMonitor.UNKNOWN);

        // configure the request with the build launch settings derived from the launch
        // configuration
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
