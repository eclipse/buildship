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
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.util.gradle.TransientRequestAttributes;

import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.i18n.CoreMessages;
import org.eclipse.buildship.core.workspace.GradleBuild;

/**
 * Runs a Gradle test build which executes a list of test classes.
 */
public final class RunGradleJvmTestLaunchRequestJob extends BaseLaunchRequestJob<TestLauncher> {

    private final ImmutableList<TestTarget> testTargets;
    private final RunConfiguration runConfig;

    public RunGradleJvmTestLaunchRequestJob(List<TestTarget> testTargets, RunConfiguration runConfig) {
        super("Launching Gradle Tests");
        this.testTargets = ImmutableList.copyOf(testTargets);
        this.runConfig = Preconditions.checkNotNull(runConfig);
    }

    @Override
    protected String getJobTaskName() {
        return "Launch Gradle test classes";
    }

    @Override
    protected RunConfiguration getRunConfig() {
        return this.runConfig;
    }

    @Override
    protected ProcessDescription createProcessDescription() {
        String processName = createProcessName(this.runConfig.getProjectConfiguration().getProjectDir());
        return new TestLaunchProcessDescription(processName);
    }

    private String createProcessName(File workingDir) {
        return String.format("[Gradle Project] %s in %s (%s)", Joiner.on(' ').join(collectSimpleNames(this.testTargets)),
                workingDir.getAbsolutePath(),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected TestLauncher createLaunch(GradleBuild gradleBuild, RunConfiguration runConfiguration, TransientRequestAttributes transientAttributes, Writer configWriter, ProcessDescription processDescription) {
        TestLauncher launcher = gradleBuild.newTestLauncher(runConfiguration, configWriter, transientAttributes);
        for (TestTarget testTarget : RunGradleJvmTestLaunchRequestJob.this.testTargets) {
            testTarget.apply(launcher);
        }
        return launcher;
    }

    @Override
    protected void executeLaunch(TestLauncher launcher) {
        launcher.run();
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_Tests, Joiner.on(' ').join(collectQualifiedNames(this.testTargets))));
    }

    /**
     * Implementation of {@code ProcessDescription}.
     */
    private final class TestLaunchProcessDescription extends BaseProcessDescription {

        public TestLaunchProcessDescription(String processName) {
            super(processName, RunGradleJvmTestLaunchRequestJob.this, RunGradleJvmTestLaunchRequestJob.this.runConfig);
        }

        @Override
        public boolean isRerunnable() {
            return true;
        }

        @Override
        public void rerun() {
            RunGradleJvmTestLaunchRequestJob job = new RunGradleJvmTestLaunchRequestJob(
                    RunGradleJvmTestLaunchRequestJob.this.testTargets,
                    RunGradleJvmTestLaunchRequestJob.this.runConfig);
            job.schedule();
        }
    }

    private static Collection<String> collectQualifiedNames(ImmutableList<TestTarget> testTargets) {
        return FluentIterable.from(testTargets).transform(new Function<TestTarget, String>() {

            @Override
            public String apply(TestTarget testTarget) {
                return testTarget.getQualifiedName();
            }
        }).toSet();
    }

    private static Collection<String> collectSimpleNames(ImmutableList<TestTarget> testTargets) {
        return FluentIterable.from(testTargets).transform(new Function<TestTarget, String>() {

            @Override
            public String apply(TestTarget testTarget) {
                return testTarget.getSimpleName();
            }
        }).toSet();
    }

}
