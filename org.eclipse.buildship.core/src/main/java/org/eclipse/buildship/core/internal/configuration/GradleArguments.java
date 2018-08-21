/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;
import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution;
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * Holds configuration values to apply on Tooling API objects.
 *
 * @author Donat Csikos
 */
public final class GradleArguments {

    private final File rootDir;
    private final GradleDistribution gradleDistribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final boolean buildScansEnabled;
    private final boolean offlineMode;
    private final List<String> arguments;
    private final List<String> jvmArguments;

    private GradleArguments(File rootDir, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, boolean buildScansEnabled, boolean offlineMode, List<String> arguments, List<String> jvmArguments) {
        this.rootDir = Preconditions.checkNotNull(rootDir);
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        this.gradleUserHome = gradleUserHome;
        this.javaHome = javaHome;
        this.buildScansEnabled = buildScansEnabled;
        this.offlineMode = offlineMode;
        this.arguments = ImmutableList.copyOf(arguments);
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
    }

    public void describe(GradleProgressAttributes progressAttributes, BuildEnvironment buildEnvironment) {
        GradleEnvironment gradleEnv = buildEnvironment.getGradle();
        JavaEnvironment javaEnv = buildEnvironment.getJava();

        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_WorkingDirectory, this.rootDir));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.Preference_Label_GradleUserHome, toNonEmpty(this.gradleUserHome != null ? this.gradleUserHome : getGradleUserHome(gradleEnv), CoreMessages.Value_UseGradleDefault)));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_GradleDistribution, this.gradleDistribution.toString()));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_GradleVersion, gradleEnv.getGradleVersion()));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_JavaHome, toNonEmpty(this.javaHome != null ? this.javaHome :  javaEnv.getJavaHome(), CoreMessages.Value_UseGradleDefault)));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_JvmArguments, toNonEmpty(this.jvmArguments, CoreMessages.Value_None)));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_Arguments, toNonEmpty(this.arguments, CoreMessages.Value_None)));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_BuildScansEnabled, this.buildScansEnabled));
        progressAttributes.writeConfig(String.format("%s: %s", CoreMessages.RunConfiguration_Label_OfflineModeEnabled, this.offlineMode));
    }

    private static File getGradleUserHome(GradleEnvironment gradleEnvironment) {
        try {
            return gradleEnvironment.getGradleUserHome();
        } catch (Exception ignore) {
            return null;
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

    public void applyTo(GradleConnector connector) {
        connector.forProjectDirectory(this.rootDir);
        connector.useGradleUserHomeDir(this.gradleUserHome);
        this.gradleDistribution.apply(connector);
    }

    public void applyTo(LongRunningOperation operation, BuildEnvironment environment) {
        operation.withArguments(collectArguments(this.arguments,
                 this.buildScansEnabled, this.offlineMode, environment));
        operation.setJavaHome(this.javaHome);
        operation.setJvmArguments(this.jvmArguments);
    }

    public static GradleArguments from(File rootDir, GradleDistribution gradleDistribution,
                                       File gradleUserHome, File javaHome,
                                       boolean buildScansEnabled,
                                       boolean offlineMode, List<String> arguments,
                                       List<String> jvmArguments) {
        return new GradleArguments(rootDir, gradleDistribution, gradleUserHome, javaHome, buildScansEnabled, offlineMode, arguments, jvmArguments);
    }


    private static List<String> collectArguments(List<String> baseArgs, boolean buildScansEnabled, boolean offlineMode, BuildEnvironment buildEnvironment) {
        List<String> arguments = Lists.newArrayList(baseArgs);
        if (buildScansEnabled) {
            String buildScanArgument = buildScanArgumentFor(buildEnvironment);
            if (!arguments.contains(buildScanArgument)) {
                arguments.add(buildScanArgument);
            }
        }
        if (offlineMode && !arguments.contains("--offline")) {
            arguments.add("--offline");
        }
        arguments.addAll(CorePlugin.invocationCustomizer().getExtraArguments());
        return arguments;
    }

    private static String buildScanArgumentFor(BuildEnvironment environment) {
        GradleVersion currentVersion = GradleVersion.version(environment.getGradle().getGradleVersion());
        GradleVersion supportsDashDashScanVersion = GradleVersion.version("3.5");

        if (supportsDashDashScanVersion.compareTo(currentVersion) <= 0) {
            return "--scan";
        } else {
            return "-Dscan";
        }
    }

}