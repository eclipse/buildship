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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;

import org.eclipse.jdt.core.IType;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.i18n.CoreMessages;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Runs a Gradle test build which executes a list of test classes.
 */
public final class RunGradleJvmTestLaunchRequestJob extends BaseLaunchRequestJob {

    private final ImmutableList<IType> testClasses;
    private final GradleRunConfigurationAttributes configurationAttributes;

    public RunGradleJvmTestLaunchRequestJob(List<IType> testClasses, GradleRunConfigurationAttributes configurationAttributes) {
        super("Launching Gradle Tests", false);
        this.testClasses = ImmutableList.copyOf(testClasses);
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
    }

    @Override
    protected String getJobTaskName() {
        return "Launch Gradle test classes";
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
        return String.format("[Gradle Project] %s in %s (%s)", Joiner.on(' ').join(collectSimpleClassNames(testClasses)),
                workingDir.getAbsolutePath(),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected Request<Void> createRequest() {
        return CorePlugin.toolingClient().newTestLaunchRequest(TestConfig.forJvmTestClasses(collectFullyQualifiedClassNames(this.testClasses)));
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_Tests, Joiner.on(' ').join(collectFullyQualifiedClassNames(testClasses))));
    }

    /**
     * Implementation of {@code ProcessDescription}.
     */
    private final class TestLaunchProcessDescription extends BaseProcessDescription {

        public TestLaunchProcessDescription(String processName) {
            super(processName, RunGradleJvmTestLaunchRequestJob.this,
                    RunGradleJvmTestLaunchRequestJob.this.configurationAttributes);
        }

        @Override
        public boolean isRerunnable() {
            return true;
        }

        @Override
        public void rerun() {
            RunGradleJvmTestLaunchRequestJob job = new RunGradleJvmTestLaunchRequestJob(
                    RunGradleJvmTestLaunchRequestJob.this.testClasses,
                    RunGradleJvmTestLaunchRequestJob.this.configurationAttributes);
            job.schedule();
        }

    }

    private static Collection<String> collectFullyQualifiedClassNames(ImmutableList<IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, String>() {

            @Override
            public String apply(IType type) {
                return type.getFullyQualifiedName();
            }
        }).toSet();
    }

    private static Collection<String> collectSimpleClassNames(ImmutableList<IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, String>() {

            @Override
            public String apply(IType type) {
                return type.getElementName();
            }
        }).toSet();
    }

}
