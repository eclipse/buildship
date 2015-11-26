/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.launch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.i18n.CoreMessages;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;

/**
 * Runs a Gradle test build which executes a list of test methods.
 */
public final class RunGradleJvmTestMethodLaunchRequestJob extends BaseLaunchRequestJob {

    private final GradleRunConfigurationAttributes configurationAttributes;
    private ImmutableMap<String, Iterable<String>> classNamesWithMethods;

    public RunGradleJvmTestMethodLaunchRequestJob(Map<String, Iterable<String>> classNamesWithMethods,
            GradleRunConfigurationAttributes configurationAttributes) {
        super("Launching Gradle Tests", false);
        this.classNamesWithMethods = ImmutableMap.copyOf(classNamesWithMethods);
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
    }

    @Override
    protected String getJobTaskName() {
        return "Launch Gradle Test Methods";
    }

    @Override
    protected GradleRunConfigurationAttributes getConfigurationAttributes() {
        return this.configurationAttributes;
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        String processName = createProcessName(this.configurationAttributes.getWorkingDir());
        return new TestLaunchProcessDescription(processName);
    }

    private String createProcessName(File workingDir) {
        return String.format("%s [Gradle Project] %s in %s (%s)",
                Joiner.on(' ').join(this.classNamesWithMethods.keySet()),
                Joiner.on(' ').join(this.classNamesWithMethods.values()), workingDir.getAbsolutePath(),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected Request<Void> createRequest() {
        TestConfig.Builder testConfig = new TestConfig.Builder();
        for (String className : classNamesWithMethods.keySet()) {
            Iterable<String> methodNames = classNamesWithMethods.get(className);
            testConfig.jvmTestMethods(className, methodNames);
        }
        return CorePlugin.toolingClient().newTestLaunchRequest(testConfig.build());
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(CoreMessages.RunConfiguration_Label_Tests);
        writer.write(": ");
        for (String className : this.classNamesWithMethods.keySet()) {
            for (String methodName : this.classNamesWithMethods.get(className)) {
                writer.write(className);
                writer.write('.');
                writer.write(methodName);
                writer.write("() ");
            }
        }
        writer.write('\n');
    }

    /**
     * Implementation of {@code ProcessDescription}.
     */
    private final class TestLaunchProcessDescription extends BaseProcessDescription {

        public TestLaunchProcessDescription(String processName) {
            super(processName, RunGradleJvmTestMethodLaunchRequestJob.this,
                    RunGradleJvmTestMethodLaunchRequestJob.this.configurationAttributes);
        }

        @Override
        public boolean isRerunnable() {
            return true;
        }

        @Override
        public void rerun() {
            RunGradleJvmTestMethodLaunchRequestJob job = new RunGradleJvmTestMethodLaunchRequestJob(
                    RunGradleJvmTestMethodLaunchRequestJob.this.classNamesWithMethods,
                    RunGradleJvmTestMethodLaunchRequestJob.this.configurationAttributes
            );
            job.schedule();
        }

    }

}
