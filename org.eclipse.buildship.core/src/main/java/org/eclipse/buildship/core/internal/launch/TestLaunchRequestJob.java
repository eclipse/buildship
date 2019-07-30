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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;

/**
 * Executes Gradle tasks based on a given {@code ILaunch} and {@code ILaunchConfiguration} instance.
 */
public final class TestLaunchRequestJob extends BaseLaunchRequestJob<TestLauncher> {

    private final ILaunchConfiguration launchConfiguration;
    private final String mode;
    private final RunConfiguration runConfig;
    private List<String> testClasses;
    private List<String> testMethods;

    public TestLaunchRequestJob(ILaunchConfiguration launchConfiguration, String mode) {
        super("Launching Gradle tests");
        this.launchConfiguration = Preconditions.checkNotNull(launchConfiguration);
        this.mode = mode;
        this.runConfig = CorePlugin.configurationManager().loadRunConfiguration(launchConfiguration);

        try {
            this.testClasses = this.launchConfiguration.getAttribute("test_classes", Collections.emptyList());
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot read run configuration", e);
            this.testClasses = Collections.emptyList();
        }

        try {
            this.testMethods = this.launchConfiguration.getAttribute("test_methods", Collections.emptyList());
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
        String processName = createProcessName(tests, this.runConfig.getProjectConfiguration().getProjectDir(), this.launchConfiguration.getName());
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
         if (this.mode.equals("debug")) { // TODO (donat) restrict debugging to Gradle 5.6+
            try {
                int port = findAvailablePort();
                ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
                ILaunch debuggerSession = startDebuggerSession(launchManager, port);
                launcher.debugTestsOn(port);
                launcher.run();
                debuggerSession.terminate();
                launchManager.removeLaunch(debuggerSession);
            } catch (CoreException e) {
                CorePlugin.logger().warn("Failed to launch debugger", e);
            }
        } else {
            launcher.run();
        }
    }

    private ILaunch startDebuggerSession(ILaunchManager launchManager, int port) throws CoreException {
        String workingDirExpr = this.launchConfiguration.getAttribute("working_dir", (String) null);
        String workingDir = ExpressionUtils.decode(workingDirExpr);
        IProject project = null;

        if (workingDir != null) {
            project = CorePlugin.workspaceOperations().findProjectByLocation(new File(workingDir)).orNull();
        }

        ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType("org.eclipse.jdt.launching.remoteJavaApplication");
        ILaunchConfigurationWorkingCopy launchConfiguration = launchConfigurationType.newInstance(null, "Gradle remote launch");
        launchConfiguration.setAttribute("org.eclipse.jdt.launching.ALLOW_TERMINATE", false);
        Map<String, String> map = new HashMap<>();
        map.put("connectionLimit", "1");
        map.put("port", String.valueOf(port));
        launchConfiguration.setAttribute("org.eclipse.jdt.launching.CONNECT_MAP", map);
        if (project != null) { // TODO what happens if project is null?
            launchConfiguration.setAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", project.getFullPath().toPortableString());
        }
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
            ServerSocket ss = new ServerSocket(port);
            try {
                ss.setReuseAddress(true);
            } finally {
                ss.close();
            }
            DatagramSocket ds = new DatagramSocket(port);
            try {
                ds.setReuseAddress(true);
            } finally {
                ds.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void writeExtraConfigInfo(GradleProgressAttributes progressAttributes) {
        progressAttributes.writeConfig(String.format("%s: %s", "Test classes", this.testClasses));
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
            CorePlugin.gradleLaunchConfigurationManager().launch(TestLaunchRequestJob.this.launchConfiguration, TestLaunchRequestJob.this.mode);
        }
    }

}

