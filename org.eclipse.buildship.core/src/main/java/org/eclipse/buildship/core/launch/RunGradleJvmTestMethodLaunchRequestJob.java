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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

import com.gradleware.tooling.toolingclient.Request;
import com.gradleware.tooling.toolingclient.TestConfig;

import org.eclipse.jdt.core.IMethod;
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
import java.util.Map;
import java.util.Map.Entry;

/**
 * Runs a Gradle test build which executes a list of test methods.
 */
public final class RunGradleJvmTestMethodLaunchRequestJob extends BaseLaunchRequestJob {

    private final ImmutableList<IMethod> testMethods;
    private final GradleRunConfigurationAttributes configurationAttributes;

    public RunGradleJvmTestMethodLaunchRequestJob(List<IMethod> testMethods,
                                                  GradleRunConfigurationAttributes configurationAttributes) {
        super("Launching Gradle Tests", false);
        this.testMethods = ImmutableList.copyOf(testMethods);
        this.configurationAttributes = Preconditions.checkNotNull(configurationAttributes);
    }

    @Override
    protected String getJobTaskName() {
        return "Launch Gradle test methods";
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
        return String.format("[Gradle Project] %s in %s (%s)",
                Joiner.on(' ').join(collectSimpleMetodSignatures(testMethods)),
                workingDir.getAbsolutePath(),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
    }

    @Override
    protected Request<Void> createRequest() {
        TestConfig.Builder testConfig = new TestConfig.Builder();
        Map<String, Collection<String>> classNamesWithMethods = collectClassNamesForMethods(testMethods);
        for (Entry<String, Collection<String>> classNameWithMethods : classNamesWithMethods.entrySet()) {
            testConfig.jvmTestMethods(classNameWithMethods.getKey(), classNameWithMethods.getValue());
        }
        return CorePlugin.toolingClient().newTestLaunchRequest(testConfig.build());
    }

    @Override
    protected void writeExtraConfigInfo(OutputStreamWriter writer) throws IOException {
        writer.write(String.format("%s: %s%n", CoreMessages.RunConfiguration_Label_Tests, Joiner.on(' ').join(collectQualifiedMetodSignatures(testMethods))));
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
                    RunGradleJvmTestMethodLaunchRequestJob.this.testMethods,
                    RunGradleJvmTestMethodLaunchRequestJob.this.configurationAttributes
            );
            job.schedule();
        }
    }

    private static Map<String, Collection<String>> collectClassNamesForMethods(List<IMethod> methods) {
        ImmutableMultimap.Builder<String, String> result = ImmutableMultimap.builder();
        for (IMethod method : methods) {
            IType declaringType = method.getDeclaringType();
            if (declaringType != null) {
                String typeName = declaringType.getFullyQualifiedName();
                String methodName = method.getElementName();
                result.put(typeName, methodName);
            }
        }
        return result.build().asMap();
    }

    private static Iterable<String> collectSimpleMetodSignatures(List<IMethod> methods) {
        ImmutableList.Builder<String> result = ImmutableList.builder();
        for (IMethod method : methods) {
            IType declaringType = method.getDeclaringType();
            if (declaringType != null) {
                result.add(declaringType.getElementName() + "#" + method.getElementName());
            }
        }
        return result.build();
    }
    
    private static Iterable<String> collectQualifiedMetodSignatures(List<IMethod> methods) {
        ImmutableList.Builder<String> result = ImmutableList.builder();
        for (IMethod method : methods) {
            IType declaringType = method.getDeclaringType();
            if (declaringType != null) {
                result.add(declaringType.getFullyQualifiedName() + "#" + method.getElementName());
            }
        }
        return result.build();
    }

}
