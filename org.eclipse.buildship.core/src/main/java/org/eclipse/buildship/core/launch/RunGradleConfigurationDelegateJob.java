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
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.internal.DefaultExecuteLaunchRequestEvent;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;

/**
 * {@link BaseLaunchRequestJob} implementation executing a
 * {@link com.gradleware.tooling.toolingclient.BuildLaunchRequest}, configured with
 * {@link GradleRunConfigurationAttributes} run configuration.
 */
public final class RunGradleConfigurationDelegateJob extends BaseLaunchRequestJob {

    private final ILaunch launch;
    private final FixedRequestAttributes fixedAttributes;
    private final GradleRunConfigurationAttributes configurationAttributes;
    private final String displayName;

    public RunGradleConfigurationDelegateJob(ILaunch launch, ILaunchConfiguration launchConfiguration) {
        super("Launching Gradle tasks");
        this.launch = Preconditions.checkNotNull(launch);
        this.configurationAttributes = GradleRunConfigurationAttributes.from(launchConfiguration);
        this.fixedAttributes = createFixedAttriubtes(this.configurationAttributes);
        this.displayName = createProcessName(this.configurationAttributes.getTasks(), this.fixedAttributes.getProjectDir(), launchConfiguration.getName());
    }

    private FixedRequestAttributes createFixedAttriubtes(GradleRunConfigurationAttributes configurationAttributes) {
        File workingDir = configurationAttributes.getWorkingDir();
        File gradleUserHome = configurationAttributes.getGradleUserHome();
        GradleDistribution gradleDistribution = configurationAttributes.getGradleDistribution();
        File javaHome = configurationAttributes.getJavaHome();
        ImmutableList<String> jvmArguments = configurationAttributes.getJvmArguments();
        ImmutableList<String> arguments = configurationAttributes.getArguments();
        return new FixedRequestAttributes(workingDir, gradleUserHome, gradleDistribution, javaHome, jvmArguments, arguments);
    }

    @Override
    protected ILaunch getLaunch() {
        return this.launch;
    }

    @Override
    protected FixedRequestAttributes getRequestAttributes() {
        return this.fixedAttributes;
    }

    @Override
    protected String getDisplayName() {
        return this.displayName;
    }

    private String createProcessName(List<String> tasks, File workingDir, String launchConfigurationName) {
        return String.format("%s [Gradle Project] %s in %s (%s)", launchConfigurationName, Joiner.on(' ').join(tasks), workingDir.getAbsolutePath(), DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected Event createEventToFireBeforeExecution(Request<Void> request) {
        return new DefaultExecuteLaunchRequestEvent(this, request, this.configurationAttributes, this.displayName);
    }

    @Override
    protected String getJobTaskName() {
        return String.format("Launch Gradle tasks %s", this.configurationAttributes.getTasks());
    }

    @Override
    protected Request<Void> createRequest() {
        return CorePlugin.toolingClient().newBuildLaunchRequest(LaunchableConfig.forTasks(this.configurationAttributes.getTasks()));
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        String taskNames = Strings.emptyToNull(CollectionsUtils.joinWithSpace(this.configurationAttributes.getTasks()));
        taskNames = taskNames != null ? taskNames : CoreMessages.RunConfiguration_Value_RunDefaultTasks;
        writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleTasks, taskNames));
    }

}
