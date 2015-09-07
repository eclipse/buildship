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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.internal.BuildExecutionParticipants;
import org.eclipse.buildship.core.launch.internal.DefaultExecuteBuildLaunchRequestEvent;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionFormatter;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

/**
 * Runs the given {@link ILaunch} instance.
 */
public final class RunGradleConfigurationDelegateJob extends ToolingApiJob {

    // todo (etst) close streams when done

    private final ILaunch launch;
    private final GradleRunConfigurationAttributes configurationAttributes;
    private final String launchName;

    private Optional<List<TestOperationDescriptor>> testOperationDescriptors = Optional.absent();

    public static final RunGradleConfigurationDelegateJob createDefaultGradleConfigurationDelegateJob(ILaunch launch,
            ILaunchConfiguration launchConfiguration) {
        GradleRunConfigurationAttributes gradleRunConfigurationAttributes = GradleRunConfigurationAttributes
                .from(launchConfiguration);
        return new RunGradleConfigurationDelegateJob(launchConfiguration.getName(), launch,
                gradleRunConfigurationAttributes);
    }

    public static final RunGradleConfigurationDelegateJob createTestReRunDelegateJob(ILaunch launch, String launchName,
            List<TestOperationDescriptor> testOperationDescriptors,
            GradleRunConfigurationAttributes configurationAttributes) {
        RunGradleConfigurationDelegateJob configurationDelegateJob = new RunGradleConfigurationDelegateJob(launchName,
                launch, configurationAttributes);
        configurationDelegateJob.setTestDescriptors(testOperationDescriptors);

        return configurationDelegateJob;
    }

    protected RunGradleConfigurationDelegateJob(String launchName, ILaunch launch,
            GradleRunConfigurationAttributes gradleRunConfigurationAttributes) {
        super("Launching Gradle tasks", false);

        this.launchName = launchName;
        this.launch = Preconditions.checkNotNull(launch);
        this.configurationAttributes = Preconditions.checkNotNull(gradleRunConfigurationAttributes);
    }

    @Override
    protected void runToolingApiJob(IProgressMonitor monitor) {
        // activate all plugins which contribute to a build execution
        BuildExecutionParticipants.activateParticipantPlugins();

        // derive all build launch settings from the launch configuration
        List<String> tasks = this.configurationAttributes.getTasks();
        File workingDir = this.configurationAttributes.getWorkingDir();
        File gradleUserHome = this.configurationAttributes.getGradleUserHome();
        GradleDistribution gradleDistribution = this.configurationAttributes.getGradleDistribution();
        File javaHome = this.configurationAttributes.getJavaHome();
        ImmutableList<String> jvmArguments = this.configurationAttributes.getJvmArguments();
        ImmutableList<String> arguments = this.configurationAttributes.getArguments();

        // start tracking progress
        monitor.beginTask(String.format("Launch Gradle tasks %s", tasks), IProgressMonitor.UNKNOWN);

        String processName = createProcessName(tasks, workingDir);
        ProcessDescription processDescription = ProcessDescription.with(processName, this.launch, this);
        ProcessStreams processStreams = CorePlugin.processStreamsProvider().createProcessStreams(processDescription);

        // fetch build environment
        List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
        FixedRequestAttributes fixedAttributes = new FixedRequestAttributes(workingDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, processStreams.getOutput(), processStreams.getError(), processStreams.getInput(), listeners,
                ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), getToken());

        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(fixedAttributes, transientAttributes, monitor);

        // configure the request's fixed attributes with the build launch settings derived from the launch configuration
        Request<?> request = null;
        if(this.testOperationDescriptors.isPresent()) {
            TestConfig testConfig = TestConfig.forTests(this.testOperationDescriptors.get()
                    .toArray(new TestOperationDescriptor[this.testOperationDescriptors.get().size()]));
            request = CorePlugin.toolingClient().newTestLaunchRequest(testConfig);
        }else {
            request = CorePlugin.toolingClient().newBuildLaunchRequest(LaunchableConfig.forTasks(tasks));
        }
        request.projectDir(workingDir);
        request.gradleUserHomeDir(gradleUserHome);
        request.gradleDistribution(gradleDistribution);
        request.javaHomeDir(javaHome);
        request.jvmArguments(jvmArguments.toArray(new String[jvmArguments.size()]));
        request.arguments(arguments.toArray(new String[arguments.size()]));

        // configure the request's transient attributes
        request.standardOutput(processStreams.getOutput());
        request.standardError(processStreams.getError());
        request.standardInput(processStreams.getInput());
        request.progressListeners(listeners.toArray(new ProgressListener[listeners.size()]));
        request.cancellationToken(getToken());

        // print the applied run configuration settings at the beginning of the console output
        writeRunConfigurationDescription(this.configurationAttributes, buildEnvironment, processStreams.getConfiguration());

        // notify the listeners before executing the build launch request
        ExecuteBuildLaunchRequestEvent event = new DefaultExecuteBuildLaunchRequestEvent(this, request, this.configurationAttributes, processName);
        CorePlugin.listenerRegistry().dispatch(event);

        // launch the build
        request.executeAndWait();
    }

    protected void setTestDescriptors(List<TestOperationDescriptor> testOperationDescriptors) {
        this.testOperationDescriptors = Optional.of(testOperationDescriptors);
    }

    private String createProcessName(List<String> tasks, File workingDir) {
        return String.format("%s [Gradle Project] %s in %s (%s)", this.launchName,
                Joiner.on(' ').join(tasks), workingDir.getAbsolutePath(),
                DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    private void writeRunConfigurationDescription(GradleRunConfigurationAttributes runConfiguration, OmniBuildEnvironment buildEnvironment, OutputStream output) {
        OutputStreamWriter writer = new OutputStreamWriter(output);

        // should the user not specify values for the gradleUserHome and javaHome, their default
        // values will not be specified in the launch configurations
        // as such, these attributes are retrieved separately from the build environment
        File gradleUserHome = runConfiguration.getGradleUserHome();
        if (gradleUserHome == null) {
            gradleUserHome = buildEnvironment.getGradle().getGradleUserHome().or(null);
        }
        File javaHome = runConfiguration.getJavaHome();
        if (javaHome == null) {
            javaHome = buildEnvironment.getJava().getJavaHome();
        }
        String gradleVersion = buildEnvironment.getGradle().getGradleVersion();

        try {
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleTasks, toNonEmpty(runConfiguration.getTasks(), CoreMessages.RunConfiguration_Value_RunDefaultTasks)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_WorkingDirectory, runConfiguration.getWorkingDir().getAbsolutePath()));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleUserHome, toNonEmpty(gradleUserHome, CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleDistribution, GradleDistributionFormatter.toString(runConfiguration.getGradleDistribution())));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleVersion, gradleVersion));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JavaHome, toNonEmpty(javaHome, CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JvmArguments, toNonEmpty(runConfiguration.getJvmArguments(), CoreMessages.Value_None)));
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

    private OmniBuildEnvironment fetchBuildEnvironment(FixedRequestAttributes fixedRequestAttributes, TransientRequestAttributes transientRequestAttributes, IProgressMonitor monitor) {
        monitor.beginTask("Load Gradle Build Environment", IProgressMonitor.UNKNOWN);
        try {
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
            return repository.fetchBuildEnvironment(transientRequestAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    public ILaunch getLaunch() {
        return launch;
    }

}
