/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Joiner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.BaseRunConfiguration;
import org.eclipse.buildship.core.internal.configuration.Test;
import org.eclipse.buildship.core.internal.configuration.TestRunConfiguration;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Executes Gradle tasks based on a given {@code ILaunch} and {@code ILaunchConfiguration} instance.
 */
public final class RunGradleJvmTestLaunchRequestJob extends BaseLaunchRequestJob<TestLauncher> {

    private final TestRunConfiguration runConfig;
    private final String name;
    private final String mode;
    private final boolean supportsTestDebugging;
    private final IProject project;

    public RunGradleJvmTestLaunchRequestJob(ILaunchConfiguration launch, String mode) {
        this(CorePlugin.configurationManager().loadTestRunConfiguration(launch), launch.getName(), mode);
    }

    private RunGradleJvmTestLaunchRequestJob(TestRunConfiguration launch, String name, String mode) {
        super("Launching Gradle tests");
        this.runConfig = launch;
        this.name = name;
        this.mode = mode;

        File projectDir = this.runConfig.getProjectConfiguration().getProjectDir();
        this.project = CorePlugin.workspaceOperations().findProjectByLocation(projectDir).orNull();

        if (this.project != null) {
            PersistentModel model = CorePlugin.modelPersistence().loadModel(this.project);
            this.supportsTestDebugging = model.isPresent() ? model.getGradleVersion().supportsTestDebugging() : false;
        } else {
            this.supportsTestDebugging = false;
        }
    }

    @Override
    protected BaseRunConfiguration getRunConfig() {
        return this.runConfig;
    }

    @Override
    protected String getJobTaskName() {
        return String.format("Launch Gradle tests %s", this.runConfig.getTests());
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        String processName = createProcessName(this.runConfig.getTests(), this.runConfig.getProjectConfiguration().getProjectDir(), this.name);
        return new BuildLaunchProcessDescription(processName);
    }

    private String createProcessName(List<Test> tests, File workingDir, String launchConfigurationName) {
        return String.format("%s [Gradle Test] %s in %s (%s)", launchConfigurationName, Joiner.on(' ').join(tests), workingDir.getAbsolutePath(), DateFormat
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
    protected void executeLaunch(TestLauncher launcher) {
         if (this.project != null && this.project.isAccessible() && launchedInDebugMode() && this.supportsTestDebugging) {
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
        } else {
            launcher.run();
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

    @Override
    protected void writeExtraConfigInfo(GradleProgressAttributes progressAttributes) {
        if (launchedInDebugMode()) {
            if (!this.supportsTestDebugging) {
                progressAttributes.writeConfig("[WARN] Current Gradle distribution does not support test debugging. Upgrade to 5.6 to make use of this feature");
            }
            if (this.project == null || !this.project.isAccessible()) {
                progressAttributes.writeConfig("[WARN] Cannot initate debugging as no accessible project present at " + this.runConfig.getProjectConfiguration().getProjectDir());
            }
        } else {
            progressAttributes.writeConfig(String.format("%s: %s", "Tests", Joiner.on(", ").join(this.runConfig.getTests())));
        }

        progressAttributes.writeConfig("");
    }

    private boolean launchedInDebugMode() {
        return "debug".equals(this.mode);
    }

    /**
     * Implementation of {@code ProcessDescription}.
     */
    private final class BuildLaunchProcessDescription extends BaseProcessDescription {

        public BuildLaunchProcessDescription(String processName) {
            super(processName, RunGradleJvmTestLaunchRequestJob.this, RunGradleJvmTestLaunchRequestJob.this.runConfig);
        }

        @Override
        public boolean isRerunnable() {
            return true;
        }

        @Override
        public void rerun() {
            RunGradleJvmTestLaunchRequestJob job = new RunGradleJvmTestLaunchRequestJob(RunGradleJvmTestLaunchRequestJob.this.runConfig, RunGradleJvmTestLaunchRequestJob.this.name, RunGradleJvmTestLaunchRequestJob.this.mode);
            job.schedule();
        }
    }
}

