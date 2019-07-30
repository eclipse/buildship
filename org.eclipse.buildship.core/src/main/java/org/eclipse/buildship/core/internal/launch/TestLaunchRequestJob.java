/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Executes Gradle tasks based on a given {@code ILaunch} and {@code ILaunchConfiguration} instance.
 */
public final class TestLaunchRequestJob extends BaseLaunchRequestJob<TestLauncher> {

    private final ILaunch launch;
    private final RunConfiguration runConfig;
    private List<String> testClasses;
    private List<String> testMethods;

    public TestLaunchRequestJob(ILaunch launch) {
        super("Launching Gradle tests");
        this.launch = Preconditions.checkNotNull(launch);
        this.runConfig = CorePlugin.configurationManager().loadRunConfiguration(launch.getLaunchConfiguration());

        try {
            this.testClasses = this.launch.getLaunchConfiguration().getAttribute("test_classes", Collections.emptyList());
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot read run configuration", e);
            this.testClasses = Collections.emptyList();
        }

        try {
            this.testMethods = this.launch.getLaunchConfiguration().getAttribute("test_methods", Collections.emptyList());
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot read run configuration", e);
            this.testMethods = Collections.emptyList();
        }
    }

    @Override
    protected String getJobTaskName() {
        List<String> tests = new ArrayList<>();
        tests.addAll(this.testClasses);
        tests.addAll(this.testMethods);
        return String.format("Launch Gradle tests %s", tests);
    }

    @Override
    protected RunConfiguration getRunConfig() {
        return this.runConfig;
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        List<String> tests = new ArrayList<>();
        tests.addAll(this.testClasses);
        tests.addAll(this.testMethods);
        String processName = createProcessName(tests, this.runConfig.getProjectConfiguration().getProjectDir(), this.launch.getLaunchConfiguration().getName());
        return new BuildLaunchProcessDescription(processName);
    }

    private String createProcessName(List<String> tests, File workingDir, String launchConfigurationName) {
        return String.format("%s [Gradle Test] %s in %s (%s)", launchConfigurationName, Joiner.on(' ').join(tests), workingDir.getAbsolutePath(), DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected TestLauncher createLaunch(InternalGradleBuild gradleBuild, RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes, ProcessDescription processDescription) {
        TestLauncher launcher = gradleBuild.newTestLauncher(this.runConfig, progressAttributes);
        launcher.withJvmTestClasses(this.testClasses);
        this.testMethods.forEach((m) -> {
            String[] parts = m.split("#");
            if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                launcher.withJvmTestMethods(parts[0], parts[1]);
            }
        });
        return launcher;
    }

    @Override
    protected void executeLaunch(TestLauncher launcher) {
        launcher.run();
    }

    @Override
    protected void writeExtraConfigInfo(GradleProgressAttributes progressAttributes) {
        progressAttributes.writeConfig(String.format("%s: %s%n", "Test classes", this.testClasses));
        progressAttributes.writeConfig(String.format("%s: %s%n", "Test methods", this.testMethods));
    }

    /**
     * Implementation of {@code ProcessDescription}.
     */
    private final class BuildLaunchProcessDescription extends BaseProcessDescription {

        public BuildLaunchProcessDescription(String processName) {
            super(processName, TestLaunchRequestJob.this, TestLaunchRequestJob.this.runConfig);
        }

        @Override
        public boolean isRerunnable() {
            return true;
        }

        @Override
        public void rerun() {
            ILaunch launch = TestLaunchRequestJob.this.launch;
            CorePlugin.gradleLaunchConfigurationManager().launch(launch.getLaunchConfiguration(), launch.getLaunchMode());
        }

    }

}

