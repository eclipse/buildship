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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.BaseProcessDescription;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.task.TaskOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Executes tests through Gradle based on a given list of {@code TestOperationDescriptor} instances and a given set of {@code GradleRunConfigurationAttributes}.
 */
public final class RunGradleTestLaunchRequestJob extends BaseLaunchRequestJob {

    private final GradleRunConfigurationAttributes configurationAttributes;
    private final List<TestOperationDescriptor> testDescriptors;

    public RunGradleTestLaunchRequestJob(GradleRunConfigurationAttributes configurationAttributes, List<TestOperationDescriptor> testDescriptors) {
        super("Launching Gradle tests");
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
        this.testDescriptors = ImmutableList.copyOf(testDescriptors);
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
        String processName = createProcessName(this.configurationAttributes.getWorkingDir());
        return new BaseProcessDescription(processName, this, this.configurationAttributes){

            @Override
            public void rerun() {
                new RunGradleTestLaunchRequestJob(RunGradleTestLaunchRequestJob.this.configurationAttributes, RunGradleTestLaunchRequestJob.this.testDescriptors).schedule();
            }
        };
    }

    private String createProcessName(File workingDir) {
        return String.format("%s [Gradle Project] %s in %s (%s)", collectTestTaskNames(this.testDescriptors), collectTestNames(this.testDescriptors),
                workingDir.getAbsolutePath(), DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    private String collectTestTaskNames(List<TestOperationDescriptor> testDescriptors) {
        ImmutableList.Builder<String> testTaskNames = ImmutableList.builder();
        for (TestOperationDescriptor testDescriptor : testDescriptors) {
            Optional<TaskOperationDescriptor> taskDescriptor = findParentTestTask(testDescriptor);
            testTaskNames.add(taskDescriptor.isPresent() ? taskDescriptor.get().getTaskPath() : "Test");
        }
        return Joiner.on(' ').join(testTaskNames.build());
    }

    private Optional<TaskOperationDescriptor> findParentTestTask(OperationDescriptor testDescriptor) {
        OperationDescriptor parent = testDescriptor.getParent();
        if (parent instanceof TaskOperationDescriptor) {
            return Optional.of((TaskOperationDescriptor) parent);
        } else if (parent != null) {
            return findParentTestTask(parent);
        } else {
            return Optional.absent();
        }
    }

    private String collectTestNames(List<TestOperationDescriptor> testDescriptors) {
        ImmutableList<String> testNames = FluentIterable.from(testDescriptors).transform(new Function<TestOperationDescriptor, String>() {

            @Override
            public String apply(TestOperationDescriptor descriptor) {
                return descriptor.getName();
            }
        }).toList();
        return Joiner.on(' ').join(testNames);
    }

    @Override
    protected Request<Void> createRequest() {
        return CorePlugin.toolingClient().newTestLaunchRequest(TestConfig.forTests(this.testDescriptors));
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(String.format("%s: %s%n", "Executed tests", collectTestDisplayNames(this.testDescriptors)));
    }

    private List<String> collectTestDisplayNames(List<TestOperationDescriptor> testDescriptors) {
        return FluentIterable.from(testDescriptors).transform(new Function<TestOperationDescriptor, String>() {

            @Override
            public String apply(TestOperationDescriptor descriptor) {
                return descriptor.getDisplayName();
            }
        }).toList();
    }

}
