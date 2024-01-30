/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.gradle.api.JavaVersion;
import org.gradle.tooling.CancellationTokenSource;

import com.google.common.base.Strings;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.operation.BaseToolingApiOperation;

/**
 * Creates the {@code java-library} Gradle template project in the target
 * directory.
 *
 * @author Donat Csikos
 */
public class InitializeNewProjectOperation extends BaseToolingApiOperation {

    private final BuildConfiguration buildConfiguration;
    private final static String CURRENT_JAVA_VERSION = JavaVersion.current().getMajorVersion();

    public InitializeNewProjectOperation(BuildConfiguration buildConfiguration) {
        super("Initialize project " + buildConfiguration.getRootProjectDirectory().getName());
        this.buildConfiguration = buildConfiguration;
    }

    @Override
    public void runInToolingApi(CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception {
        initProjectIfNotExists(this.buildConfiguration, tokenSource, monitor);

    }

    private static void initProjectIfNotExists(BuildConfiguration buildConfig, CancellationTokenSource tokenSource, IProgressMonitor monitor) {
        File projectDir = buildConfig.getRootProjectDirectory().getAbsoluteFile();
        if (!projectDir.exists()) {
            if (projectDir.mkdir()) {
                InternalGradleBuild gradleBuild = CorePlugin.internalGradleWorkspace().getGradleBuild(buildConfig);
                RunConfiguration runConfiguration = CorePlugin.configurationManager().createDefaultRunConfiguration(buildConfig);
                GradleProgressAttributes progressAttributes = GradleProgressAttributes.builder(tokenSource, gradleBuild, monitor)
                        .forNonInteractiveBackgroundProcess()
                        .withFilteredProgress()
                        .build();
                gradleBuild.newBuildLauncher(runConfiguration, progressAttributes).forTasks(createInitJavaLibraryTask(runConfiguration.getProjectConfiguration())).run();
            }
        }
    }

    private static String[] createInitJavaLibraryTask(ProjectConfiguration projectConfiguration) {
        String escapedPackageName = Arrays.stream(projectConfiguration.getProjectDir().getName().split("\\."))
                .filter(segment -> !Strings.isNullOrEmpty(segment))
                .map(InitializeNewProjectOperation::escapePackageNameSegment)
                .collect(Collectors.joining("."));
        return new String[] { "init", "--type", "java-library", "--package", escapedPackageName };
    }

    private static String escapePackageNameSegment(String packageNameSegment) {
        IStatus status = JavaConventions.validatePackageName(packageNameSegment, CURRENT_JAVA_VERSION, CURRENT_JAVA_VERSION);
        if (status.getCode() == IStatus.OK) {
            return packageNameSegment;
        } else {
            return "_" + packageNameSegment;
        }
    }

    @Override
    public ISchedulingRule getRule() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
