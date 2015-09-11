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

import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessDescription;

/**
 * {@link BaseLaunchRequestJob} implementation executing a
 * {@link com.gradleware.tooling.toolingclient.BuildLaunchRequest}.
 */
public final class RunGradleTestLaunchRequestJob extends BaseLaunchRequestJob {

    private final GradleRunConfigurationAttributes configurationAttributes;
    private final List<TestOperationDescriptor> testDescriptors;

    public RunGradleTestLaunchRequestJob(GradleRunConfigurationAttributes configurationAttributes, List<TestOperationDescriptor> testDescriptors) {
        super("Launching Gradle tests");
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
        this.testDescriptors = Preconditions.checkNotNull(testDescriptors);
    }

    @Override
    protected String getJobTaskName() {
        return "Launch Gradle tests";
    }

    @Override
    protected GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        String processName = createProcessName(this.configurationAttributes.getWorkingDir(), "name");
        return ProcessDescription.with(processName, null, this);
    }

    private String createProcessName(File workingDir, String launchConfigurationName) {
        return String.format("%s [Gradle Project] %s in %s (%s)", launchConfigurationName, Joiner.on(' ').join(collectTestNames(this.testDescriptors)), workingDir
                .getAbsolutePath(), DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected Request<Void> createRequest() {
        return CorePlugin.toolingClient().newTestLaunchRequest(TestConfig.forTests(this.testDescriptors));
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(String.format("%s: %s%n", "Executed tests", collectTestDisplayNames(this.testDescriptors)));
    }

    private static List<String> collectTestNames(List<TestOperationDescriptor> testDescriptors) {
        return FluentIterable.from(testDescriptors).transform(new Function<TestOperationDescriptor, String>() {

            @Override
            public String apply(TestOperationDescriptor descriptor) {
                return descriptor.getName();
            }
        }).toList();
    }

    private static List<String> collectTestDisplayNames(List<TestOperationDescriptor> testDescriptors) {
        return FluentIterable.from(testDescriptors).transform(new Function<TestOperationDescriptor, String>() {

            @Override
            public String apply(TestOperationDescriptor descriptor) {
                return descriptor.getDisplayName();
            }
        }).toList();
    }

}
