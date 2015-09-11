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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.gradleware.tooling.toolingclient.LaunchableConfig;
import com.gradleware.tooling.toolingclient.Request;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.launch.internal.DefaultExecuteLaunchRequestEvent;
import org.eclipse.buildship.core.util.collections.CollectionsUtils;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link BaseLaunchRequestJob} implementation executing a
 * {@link com.gradleware.tooling.toolingclient.BuildLaunchRequest}, configured with
 * {@link GradleRunConfigurationAttributes} run configuration.
 */
public final class RunGradleConfigurationDelegateJob extends BaseLaunchRequestJob {

    private final ILaunch launch;
    private final GradleRunConfigurationAttributes configurationAttributes;
    private final String displayName;

    public RunGradleConfigurationDelegateJob(ILaunch launch, ILaunchConfiguration launchConfiguration) {
        super("Launching Gradle tasks");
        this.launch = Preconditions.checkNotNull(launch);
        this.configurationAttributes = GradleRunConfigurationAttributes.from(launchConfiguration);
        this.displayName = createProcessName(this.configurationAttributes.getTasks(), this.configurationAttributes.getWorkingDir(), launchConfiguration.getName());
    }

    private String createProcessName(List<String> tasks, File workingDir, String launchConfigurationName) {
        return String.format("%s [Gradle Project] %s in %s (%s)", launchConfigurationName, Joiner.on(' ').join(tasks), workingDir.getAbsolutePath(), DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected String getJobTaskName() {
        return String.format("Launch Gradle tasks %s", this.configurationAttributes.getTasks());
    }

    @Override
    protected GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        return ProcessDescription.with(this.displayName, this.launch, this);
    }

    @Override
    protected Request<Void> createRequest() {
        return CorePlugin.toolingClient().newBuildLaunchRequest(LaunchableConfig.forTasks(this.configurationAttributes.getTasks()));
    }

    @Override
    protected Event createEventToFireBeforeExecution(Request<Void> request) {
        return new DefaultExecuteLaunchRequestEvent(this, request, this.configurationAttributes, this.displayName);
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        String taskNames = Strings.emptyToNull(CollectionsUtils.joinWithSpace(this.configurationAttributes.getTasks()));
        taskNames = taskNames != null ? taskNames : CoreMessages.RunConfiguration_Value_RunDefaultTasks;
        writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_GradleTasks, taskNames));
    }

}
