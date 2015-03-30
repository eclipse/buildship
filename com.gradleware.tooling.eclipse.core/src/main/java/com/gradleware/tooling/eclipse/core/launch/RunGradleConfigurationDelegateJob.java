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

package com.gradleware.tooling.eclipse.core.launch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.gradle.tooling.BuildCancelledException;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.TestProgressEvent;
import org.gradle.tooling.TestProgressListener;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.eclipse.core.CorePlugin;
import com.gradleware.tooling.eclipse.core.GradlePluginsRuntimeException;
import com.gradleware.tooling.eclipse.core.console.ProcessDescription;
import com.gradleware.tooling.eclipse.core.console.ProcessStreams;
import com.gradleware.tooling.eclipse.core.console.internal.CompositeProcessStream;
import com.gradleware.tooling.eclipse.core.console.internal.FileInputProcessStream;
import com.gradleware.tooling.eclipse.core.console.internal.FileOutputProcessStream;
import com.gradleware.tooling.eclipse.core.gradle.GradleDistributionFormatter;
import com.gradleware.tooling.eclipse.core.i18n.CoreMessages;
import com.gradleware.tooling.eclipse.core.testprogress.GradleTestRunSession;
import com.gradleware.tooling.eclipse.core.testprogress.GradleTestRunSessionFactory;
import com.gradleware.tooling.eclipse.core.util.collections.CollectionsUtils;
import com.gradleware.tooling.eclipse.core.util.file.FileUtils;
import com.gradleware.tooling.eclipse.core.util.progress.DelegatingProgressListener;
import com.gradleware.tooling.eclipse.core.util.progress.ToolingApiJob;
import com.gradleware.tooling.eclipse.core.util.variable.ExpressionUtils;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.ToolingClient;

/**
 * Runs the given {@link ILaunch} instance.
 */
public final class RunGradleConfigurationDelegateJob extends ToolingApiJob {

    // todo (etst) close streams when done

    private final ILaunch launch;
    private final ILaunchConfiguration launchConfiguration;

    public RunGradleConfigurationDelegateJob(ILaunch launch, ILaunchConfiguration launchConfiguration) {
        super("Launching Gradle tasks");

        this.launch = Preconditions.checkNotNull(launch);
        this.launchConfiguration = Preconditions.checkNotNull(launchConfiguration);
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

    @SuppressWarnings("unchecked")
    public void runLaunchConfiguration(IProgressMonitor monitor) {
        // derive all build launch settings from the launch configuration
        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.from(this.launchConfiguration);
        List<String> tasks = configurationAttributes.getTasks();
        File workingDir = configurationAttributes.getWorkingDir();
        GradleDistribution gradleDistribution = configurationAttributes.getGradleDistribution();
        File gradleUserHome = configurationAttributes.getGradleUserHome();
        File javaHome = configurationAttributes.getJavaHome();
        ImmutableList<String> jvmArguments = configurationAttributes.getJvmArguments();
        ImmutableList<String> arguments = configurationAttributes.getArguments();

        // start tracking progress
        monitor.beginTask(String.format("Launch Gradle tasks %s", tasks), IProgressMonitor.UNKNOWN);

        // configure the request with the build launch settings derived from the launch
        // configuration
        BuildLaunchRequest request = ToolingClient.newClient().newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));
        request.projectDir(workingDir);
        request.gradleDistribution(gradleDistribution);
        request.gradleUserHomeDir(gradleUserHome);
        request.javaHomeDir(javaHome);
        request.jvmArguments(jvmArguments.toArray(new String[jvmArguments.size()]));
        request.arguments(arguments.toArray(new String[arguments.size()]));

        // configure the request with the transient request attributes
        String processName = createProcessName(tasks, workingDir);
        ProcessDescription processDescription = ProcessDescription.with(processName, this.launch, this);
        ProcessStreams processStreams = getProcessStream(processDescription);
        request.standardOutput(processStreams.getOutput());
        request.standardError(processStreams.getError());
        request.standardInput(processStreams.getInput());
        request.progressListeners(new DelegatingProgressListener(monitor));
        request.cancellationToken(getToken());

        // print the applied run configuration settings at the beginning of the console output
        writeRunConfigurationDescription(configurationAttributes, processStreams.getOutput());

        // attach a test progress listener if the run configuration has the test progress
        // visualization enabled
        Optional<GradleTestRunSessionForwardingTestProgressListener> testProgressListener = createTestProgressListenerIfEnabled(configurationAttributes);
        if (testProgressListener.isPresent()) {
            request.testProgressListeners(testProgressListener.get());
        }

        // launch the build (optionally with test execution progress being tracked)
        if (testProgressListener.isPresent()) {
            testProgressListener.get().start();
        }
        try {
            request.executeAndWait();
        } finally {
            if (testProgressListener.isPresent()) {
                testProgressListener.get().finish();
            }
        }
    }

    private ProcessStreams getProcessStream(ProcessDescription processDescription) {
        String outputFileLocation = null; // null if the setting is not enabled
        String inputFileLocation = null;
        boolean appendToFile = false;
        try {
            inputFileLocation = this.launchConfiguration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String) null);
            appendToFile = this.launchConfiguration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
            outputFileLocation = this.launchConfiguration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String) null);
            outputFileLocation = outputFileLocation == null ? null :ExpressionUtils.decode(outputFileLocation);
            inputFileLocation = inputFileLocation == null ? null : ExpressionUtils.decode(inputFileLocation);
        } catch (CoreException e) {
            CorePlugin.logger().error("Failed to load attributes launch configuration attributes", e);
        }

        ProcessStreams baseProcessStream = CorePlugin.processStreamsProvider().createProcessStreams(processDescription);
        if (outputFileLocation == null) {
            return baseProcessStream;
        } else {
            File outputFile = new File(outputFileLocation);
            FileOutputProcessStream fileProcessStream = new FileOutputProcessStream(outputFile, appendToFile);

            ProcessStreams inputProcessStream = inputFileLocation == null ? baseProcessStream : new FileInputProcessStream(new File(inputFileLocation));
            return new CompositeProcessStream(inputProcessStream, fileProcessStream, fileProcessStream);
        }
    }

    private String createProcessName(List<String> tasks, File workingDir) {
        return String.format("%s [Gradle Project] %s in %s (%s)", this.launchConfiguration.getName(), Joiner.on(' ').join(tasks), workingDir.getAbsolutePath(), DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    private void writeRunConfigurationDescription(GradleRunConfigurationAttributes runConfiguration, OutputStream output) {
        OutputStreamWriter writer = new OutputStreamWriter(output);
        try {
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleTasks,
                    toNonEmpty(runConfiguration.getTasks(), CoreMessages.RunConfiguration_Value_RunDefaultTasks)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_WorkingDirectory, FileUtils.getAbsolutePath(runConfiguration.getWorkingDir()).get()));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleDistribution,
                    GradleDistributionFormatter.toString(runConfiguration.getGradleDistribution())));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleUserHome,
                    toNonEmpty(runConfiguration.getGradleUserHome(), CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JavaHome, toNonEmpty(runConfiguration.getJavaHome(), CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JvmArguments,
                    toNonEmpty(runConfiguration.getJvmArguments(), CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_Arguments, toNonEmpty(runConfiguration.getArguments(), CoreMessages.Value_None)));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            String message = String.format("Cannot write run configuration description to Gradle console.");
            CorePlugin.logger().error(message, e);
            throw new GradlePluginsRuntimeException(message, e);
        }
    }

    private String toNonEmpty(File fileValue, String defaultMessage) {
        String string = FileUtils.getAbsolutePath(fileValue).orNull();
        return string != null ? string : defaultMessage;
    }

    private String toNonEmpty(List<String> stringValues, String defaultMessage) {
        String string = Strings.emptyToNull(CollectionsUtils.joinWithSpace(stringValues));
        return string != null ? string : defaultMessage;
    }

    private Optional<GradleTestRunSessionForwardingTestProgressListener> createTestProgressListenerIfEnabled(GradleRunConfigurationAttributes configurationAttributes) {
        if (configurationAttributes.isVisualizeTestProgress()) {
            Optional<IJavaProject> workspaceProject = findJavaProjectInWorkspace(configurationAttributes);
            GradleTestRunSessionForwardingTestProgressListener testProgressListener = new GradleTestRunSessionForwardingTestProgressListener(this.launch, workspaceProject);
            return Optional.of(testProgressListener);
        } else {
            return Optional.absent();
        }
    }

    private Optional<IJavaProject> findJavaProjectInWorkspace(GradleRunConfigurationAttributes configuration) {
        final File workingDirectory = configuration.getWorkingDir();
        Optional<IProject> javaProject = FluentIterable.from(CorePlugin.workspaceOperations().getAllProjects()).firstMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                try {
                    return project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.getLocation().toFile().equals(workingDirectory);
                } catch (Exception e) {
                    return false;
                }
            }
        });

        return Optional.fromNullable(JavaCore.create(javaProject.orNull()));
    }

    /**
     * {@code TestProgressListener} that forwards all test progress events to a {@code GradleTestRunSession}.
     */
    public static final class GradleTestRunSessionForwardingTestProgressListener implements TestProgressListener {

        private final GradleTestRunSession session;
        private final AtomicBoolean firstInvocation;

        public GradleTestRunSessionForwardingTestProgressListener(ILaunch launch, Optional<IJavaProject> workspaceProject) {
            this.session = GradleTestRunSessionFactory.newSession(launch, workspaceProject.orNull());
            this.firstInvocation = new AtomicBoolean(true);
        }

        public void start() {
            this.session.start();
        }

        public void finish() {
            this.session.finish();
        }

        @Override
        public void statusChanged(TestProgressEvent event) {
            // activate the Test View the first time we receive a test progress event
            if (this.firstInvocation.getAndSet(false)) {
                CorePlugin.workbenchOperations().activateTestRunnerView();
            }

            this.session.process(event);
        }

    }

}
