/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Joiner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.BaseRunConfiguration;
import org.eclipse.buildship.core.internal.configuration.Test;
import org.eclipse.buildship.core.internal.configuration.TestRunConfiguration;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Executes Gradle tasks based on a given {@code ILaunch} and {@code ILaunchConfiguration} instance.
 */
public abstract class RunGradleJvmTestLaunchRequestJob extends BaseLaunchRequestJob<TestLauncher> {

    protected final String configName;
    protected final TestRunConfiguration runConfig;

    private RunGradleJvmTestLaunchRequestJob(String jobName, String configName, TestRunConfiguration runConfig) {
        super(jobName);
        this.configName = configName;
        this.runConfig = runConfig;
    }

    @Override
    protected BaseRunConfiguration getRunConfig() {
        return this.runConfig;
    }

    protected String createProcessName() {
        return String.format("%s [Gradle Test] %s in %s (%s)", this.configName, Joiner.on(' ').join(this.runConfig.getTests()), this.runConfig.getProjectConfiguration().getProjectDir().getAbsolutePath(), DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected TestLauncher createLaunch(InternalGradleBuild gradleBuild, GradleProgressAttributes progressAttributes, ProcessDescription processDescription) {
        TestLauncher launcher = gradleBuild.newTestLauncher(this.runConfig, progressAttributes);
        for(Test test : this.runConfig.getTests()) {
           test.apply(launcher);
        }
        return launcher;
    }

    @Override
    protected void writeExtraConfigInfo(GradleProgressAttributes progressAttributes) {
        progressAttributes.writeConfig(String.format("%s: %s", "Tests", Joiner.on(", ").join(this.runConfig.getTests())));
    }

    private static class RunLaunchRequestJob extends RunGradleJvmTestLaunchRequestJob {

        protected RunLaunchRequestJob(String configName, TestRunConfiguration runConfig) {
            super("Launch Gradle tests", configName, runConfig);
        }

        @Override
        protected ProcessDescription createProcessDescription() {
            return new ProcessDescription() {

                @Override
                public void rerun() {
                    new RunLaunchRequestJob(RunLaunchRequestJob.this.configName, RunLaunchRequestJob.this.runConfig).schedule();
                }

                @Override
                public boolean isRerunnable() {
                    return true;
                }

                @Override
                public BaseRunConfiguration getRunConfig() {
                    return RunLaunchRequestJob.this.runConfig;
                }

                @Override
                public String getName() {
                    return createProcessName();
                }

                @Override
                public Job getJob() {
                    return RunLaunchRequestJob.this;
                }
            };
        }

        @Override
        protected void executeLaunch(TestLauncher launcher) {
            launcher.run();
        }
    }

    private static class DebugLaunchRequestJob extends RunGradleJvmTestLaunchRequestJob {

        private final IProject project;

        protected DebugLaunchRequestJob(String configName, TestRunConfiguration runConfig, IProject project) {
            super("Debug Gradle tests", configName, runConfig);
            this.project = project;
        }

        @Override
        protected ProcessDescription createProcessDescription() {
            return new ProcessDescription() {

                @Override
                public void rerun() {
                    new DebugLaunchRequestJob(DebugLaunchRequestJob.this.configName, DebugLaunchRequestJob.this.runConfig, DebugLaunchRequestJob.this.project).schedule();
                }

                @Override
                public boolean isRerunnable() {
                    return true;
                }

                @Override
                public BaseRunConfiguration getRunConfig() {
                    return DebugLaunchRequestJob.this.runConfig;
                }

                @Override
                public String getName() {
                    return createProcessName();
                }

                @Override
                public Job getJob() {
                    return DebugLaunchRequestJob.this;
                }
            };
        }

        @Override
        protected void executeLaunch(TestLauncher launcher) {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunch debuggerSession = null;
            try {
                int port = findAvailablePort();
                debuggerSession = startDebuggerSession(launchManager, port);
                launcher.debugTestsOn(port);
                launcher.run();
           } catch (CoreException e) {
               CorePlugin.logger().warn("Failed to launch debugger", e);
           } finally {
               if (debuggerSession != null) {
                   try {
                       debuggerSession.terminate();
                   } catch (DebugException ignore) {
                   }
                   launchManager.removeLaunch(debuggerSession);
               }
           }
        }

        private ILaunch startDebuggerSession(ILaunchManager launchManager, int port) throws CoreException {
            ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType("org.eclipse.jdt.launching.remoteJavaApplication");
            ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, "Gradle remote launch");
            launchConfiguration.setAttribute("org.eclipse.jdt.launching.ALLOW_TERMINATE", false);
            Map<String, String> map = new HashMap<>();
            map.put("connectionLimit", "1");
            map.put("port", String.valueOf(port));
            launchConfiguration.setAttribute("org.eclipse.jdt.launching.CONNECT_MAP", map);
            launchConfiguration.setAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", this.project.getFullPath().toPortableString());
            launchConfiguration.setAttribute("org.eclipse.jdt.launching.VM_CONNECTOR_ID", "org.eclipse.jdt.launching.socketListenConnector");
            return launchConfiguration.launch("debug", new NullProgressMonitor());
        }

        private static int findAvailablePort() {
            for (int p = 1024; p < 65535; p++) {
                if (isAvailable(p)) {
                    return p;
                }
            }
            throw new GradlePluginsRuntimeException("Cannot find available port for debugging");
        }

        private static boolean isAvailable(int port) {
            try {
                try(ServerSocket ss = new ServerSocket(port)) {
                    ss.setReuseAddress(true);
                }
                try(DatagramSocket ds = new DatagramSocket(port)) {
                    ds.setReuseAddress(true);
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static Optional<BaseLaunchRequestJob<TestLauncher>> createJob(ILaunchConfiguration launch, String mode) {
        // launch invalid configuration
        TestRunConfiguration runConfig = null;
        try {
            runConfig = CorePlugin.configurationManager().loadTestRunConfiguration(launch);
        } catch (Exception e) {
            CorePlugin.logger().warn("Invalid launch configuration", e);
            return Optional.empty();
        }

        File projectDir = runConfig.getProjectConfiguration().getProjectDir();
        IProject project = CorePlugin.workspaceOperations().findProjectByLocation(projectDir).orNull();

        TestExecutionTarget testTarget = TestExecutionTarget.from(project, runConfig.getTests(), mode);
        Optional<String> errorMessage = testTarget.validate();

        if (errorMessage.isPresent()) {
            CorePlugin.logger().warn(errorMessage.get());
            return Optional.empty();
        }

        if ("debug".equals(mode)) {
            return Optional.of(new DebugLaunchRequestJob(launch.getName(), runConfig, project));

        } else {
            return Optional.of(new RunLaunchRequestJob(launch.getName(), runConfig));
        }
   }

    public static Optional<BaseLaunchRequestJob<TestLauncher>> createJob(JavaElementSelection selection, String mode) {
        TestExecutionTarget testTarget = TestExecutionTarget.from(selection, mode);
        Optional<String> errorMessage = testTarget.validate();

        if (errorMessage.isPresent()) {
            CorePlugin.logger().warn(errorMessage.get());
            return Optional.empty();
        }

        GradleTestRunConfigurationAttributes attributes = createLaunchConfigAttributes(testTarget.getProject(), selection.resolveTests());
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateTestRunConfiguration(attributes);
        return createJob(launchConfiguration, mode);
   }

    private static GradleTestRunConfigurationAttributes createLaunchConfigAttributes(IProject project, List<String> tests) {
        return new GradleTestRunConfigurationAttributes(ExpressionUtils.encodeWorkspaceLocation(project),
                                                    GradleDistribution.fromBuild().toString(),
                                                    null,
                                                    null,
                                                    Collections.emptyList(),
                                                    Collections.emptyList(),
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    tests);
    }
}
