/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;


/**
 * Provides capability to read and save workspace configuration.
 *
 * @author Donat Csikos
 */
final class WorkspaceConfigurationPersistence {

    private static final String GRADLE_DISTRIBUTION = "gradle.distribution";
    private static final String GRADLE_USER_HOME = "gradle.user.home";
    private static final String JAVA_HOME = "java.home";
    private static final String GRADLE_OFFLINE_MODE = "gradle.offline.mode";
    private static final String GRADLE_BUILD_SCANS = "gradle.build.scans";
    private static final String GRADLE_AUTO_SYNC = "auto.sync";
    private static final String ARGUMENTS = "arguments";
    private static final String JVM_ARGUMENTS = "jvm.arguments";
    private static final String SHOW_CONSOLE_VIEW = "show.console.view";
    private static final String SHOW_EXECUTIONS_VIEW = "show.executions.view";
    private static final String EXPERIMENTAL_ENABLE_MODULE_SUPPORT = "experimental.module.support";

    public WorkspaceConfiguration readWorkspaceConfig() {
        IEclipsePreferences preferences = getPreferences();
        String distributionString = preferences.get(GRADLE_DISTRIBUTION, null);
        GradleDistribution distribution;
        try {
            distribution = GradleDistribution.fromString(distributionString);
        } catch (RuntimeException ignore) {
            distribution = GradleDistribution.fromBuild();
        }
        String gradleUserHomeString = preferences.get(GRADLE_USER_HOME, null);
        File gradleUserHome = gradleUserHomeString == null
                ? null
                : new File(gradleUserHomeString);
        String javaHomeString = preferences.get(JAVA_HOME, null);
        File javaHome = javaHomeString == null
                ? null
                : new File(javaHomeString);
        boolean offlineMode = preferences.getBoolean(GRADLE_OFFLINE_MODE, false);
        boolean buildScansEnabled = preferences.getBoolean(GRADLE_BUILD_SCANS, false);
        boolean autoSyncEnabled = preferences.getBoolean(GRADLE_AUTO_SYNC, false);
        String argumentsString = preferences.get(ARGUMENTS, "");
        List<String> arguments = Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(argumentsString);
        String jvmArgumentsString = preferences.get(JVM_ARGUMENTS, "");
        List<String> jvmArguments = Splitter.on(File.pathSeparator).omitEmptyStrings().splitToList(jvmArgumentsString);
        boolean showConsoleView = preferences.getBoolean(SHOW_CONSOLE_VIEW, true);
        boolean showExecutionsView = preferences.getBoolean(SHOW_EXECUTIONS_VIEW, true);
        boolean moduleSupport = preferences.getBoolean(EXPERIMENTAL_ENABLE_MODULE_SUPPORT, false);

        return new WorkspaceConfiguration(distribution, gradleUserHome, javaHome, offlineMode, buildScansEnabled, autoSyncEnabled, arguments, jvmArguments, showConsoleView, showExecutionsView, moduleSupport);
    }

    public void saveWorkspaceConfiguration(WorkspaceConfiguration config) {
        Preconditions.checkNotNull(config);
        IEclipsePreferences preferences = getPreferences();
        preferences.put(GRADLE_DISTRIBUTION, config.getGradleDistribution().toString());
        if (config.getGradleUserHome() == null) {
            preferences.remove(GRADLE_USER_HOME);
        } else {
            preferences.put(GRADLE_USER_HOME, config.getGradleUserHome().getPath());
        }
        if (config.getJavaHome() == null) {
            preferences.remove(JAVA_HOME);
        } else {
            preferences.put(JAVA_HOME, config.getJavaHome().getPath());
        }
        preferences.putBoolean(GRADLE_OFFLINE_MODE, config.isOffline());
        preferences.putBoolean(GRADLE_BUILD_SCANS, config.isBuildScansEnabled());
        preferences.putBoolean(GRADLE_AUTO_SYNC, config.isAutoSync());
        preferences.put(ARGUMENTS, Joiner.on(File.pathSeparator).join(config.getArguments()));
        preferences.put(JVM_ARGUMENTS, Joiner.on(File.pathSeparator).join(config.getJvmArguments()));
        preferences.putBoolean(SHOW_CONSOLE_VIEW, config.isShowConsoleView());
        preferences.putBoolean(SHOW_EXECUTIONS_VIEW, config.isShowExecutionsView());
        preferences.putBoolean(EXPERIMENTAL_ENABLE_MODULE_SUPPORT, config.isExperimentalModuleSupportEnabled());
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new GradlePluginsRuntimeException("Could not persist workspace preferences", e);
        }
    }

    private IEclipsePreferences getPreferences() {
        return InstanceScope.INSTANCE.getNode(CorePlugin.PLUGIN_ID);
    }
}
