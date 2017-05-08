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
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.internal.BuildExecutionParticipants;
import org.eclipse.buildship.core.launch.internal.DefaultExecuteLaunchRequestEvent;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.gradle.GradleDistributionFormatter;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.ModelProvider;

/**
 * Base class to execute Gradle builds in a job.
 *
 * @param <T> the operation type the subclasses can create and execute
 */
public abstract class BaseLaunchRequestJob<T extends LongRunningOperation> extends ToolingApiJob {

    protected BaseLaunchRequestJob(String name, boolean notifyUserAboutBuildFailures) {
        super(name, notifyUserAboutBuildFailures);
    }

    @Override
    protected final void runToolingApiJob(final IProgressMonitor monitor) throws Exception {
        // todo (etst) close streams when done

        BuildExecutionParticipants.activateParticipantPlugins();
        monitor.beginTask(getJobTaskName(), IProgressMonitor.UNKNOWN);

        ProcessDescription processDescription = createProcessDescription();
        ProcessStreams processStreams = CorePlugin.processStreamsProvider().createProcessStreams(processDescription);

        RunConfiguration runConfig = getRunConfig();
        List<ProgressListener> listeners = ImmutableList.<ProgressListener>of(DelegatingProgressListener.withFullOutput(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, processStreams.getOutput(), processStreams.getError(), processStreams.getInput(),
                listeners, Collections.<org.gradle.tooling.events.ProgressListener>emptyList(), getToken());

        GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(runConfig);

        // apply FixedRequestAttributes on build launcher
        T launcher = createLaunch(gradleBuild, transientAttributes, processDescription);

        // let participants add listeners to the build
        Event event = new DefaultExecuteLaunchRequestEvent(processDescription, launcher);
        CorePlugin.listenerRegistry().dispatch(event);

        // print the applied run configuration settings at the beginning of the console output
        OutputStreamWriter writer = new OutputStreamWriter(processStreams.getConfiguration());
        writeConfig(runConfig, writer, monitor);

        // execute the build
        executeLaunch(launcher);
    }

    private void writeConfig(RunConfiguration runConfig, OutputStreamWriter writer, IProgressMonitor monitor) {
        BuildConfiguration buildConfig = runConfig.getBuildConfiguration();
        WorkspaceConfiguration workspaceConfig = buildConfig.getWorkspaceConfiguration();
        OmniBuildEnvironment buildEnvironment = fetchBuildEnvironment(runConfig, monitor);
        // should the user not specify values for the gradleUserHome and javaHome, their default
        // values will not be specified in the launch configurations
        // as such, these attributes are retrieved separately from the build environment
        File gradleUserHome = workspaceConfig.getGradleUserHome();
        if (gradleUserHome == null) {
            gradleUserHome = buildEnvironment.getGradle().getGradleUserHome().or(null);
        }
        File javaHome = runConfig.getJavaHome();
        if (javaHome == null) {
            javaHome = buildEnvironment.getJava().getJavaHome();
        }
        String gradleVersion = buildEnvironment.getGradle().getGradleVersion();

        try {
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_WorkingDirectory, buildConfig.getRootProjectDirectory().getAbsolutePath()));
            writer.write(String.format("%s: %s%n", CoreMessages.Preference_Label_GradleUserHome, toNonEmpty(gradleUserHome, CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleDistribution, GradleDistributionFormatter.toString(runConfig.getGradleDistribution())));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleVersion, gradleVersion));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JavaHome, toNonEmpty(javaHome, CoreMessages.Value_UseGradleDefault)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_JvmArguments, toNonEmpty(runConfig.getJvmArguments(), CoreMessages.Value_None)));
            writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_Arguments, toNonEmpty(runConfig.getArguments(), CoreMessages.Value_None)));
            writeExtraConfigInfo(writer);
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new GradlePluginsRuntimeException("Cannot write run configuration description to Gradle console.", e);
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

    private OmniBuildEnvironment fetchBuildEnvironment(RunConfiguration runConfiguration, IProgressMonitor monitor) {
        ModelProvider modelProvider = CorePlugin.gradleWorkspaceManager().getGradleBuild(runConfiguration).getModelProvider();
        return modelProvider.fetchBuildEnvironment(FetchStrategy.FORCE_RELOAD, getToken(), monitor);
    }

    /**
     * The name of the job to display in the progress view.
     *
     * @return the name of the job
     */
    protected abstract String getJobTaskName();

    /**
     * The run configuration attributes to apply when executing the request.
     *
     * @return the run configuration attributes
     */
    protected abstract RunConfiguration getRunConfig();

    /**
     * The process description.
     *
     * @return the process description
     */
    protected abstract ProcessDescription createProcessDescription();

    /**
     * Creates a new launcher object to execute in the job.
     *
     * @return the new launcher
     */
    protected abstract T createLaunch(GradleBuild gradleBuild, TransientRequestAttributes transientAttributes, ProcessDescription processDescription);

    /**
     * Execute the launcher created by {@code #createLaunch()}.
     *
     * @param launcher the launcher to execute
     */
    protected abstract void executeLaunch(T launcher);

    /**
     * Writes extra information on the configuration console.
     *
     * @param writer the writer to print messages with
     * @throws IOException if an exception happens when writing a message
     */
    protected abstract void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException;

    /**
     * Convenience implementation of the ProcessDescription interface.
     */
    protected abstract static class BaseProcessDescription implements ProcessDescription {

        private final String name;
        private final Job job;
        private final RunConfiguration runConfig;

        protected BaseProcessDescription(String name, Job job, RunConfiguration runConfig) {
            this.name = Preconditions.checkNotNull(name);
            this.job = Preconditions.checkNotNull(job);
            this.runConfig = Preconditions.checkNotNull(runConfig);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Job getJob() {
            return this.job;
        }

        @Override
        public RunConfiguration getRunConfig() {
            return this.runConfig;
        }

    }
}
