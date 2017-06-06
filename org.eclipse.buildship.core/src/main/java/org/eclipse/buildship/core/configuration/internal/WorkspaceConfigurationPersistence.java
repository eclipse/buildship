/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.GradleDistribution;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionSerializer;


/**
 * Provides capability to read and save workspace configuration.
 *
 * @author Donat Csikos
 */
public final class WorkspaceConfigurationPersistence {

    private static final String GRADLE_DISTRIBUTION = "gradle.distribution";
    private static final String GRADLE_USER_HOME = "gradle.user.home";
    private static final String GRADLE_OFFLINE_MODE = "gradle.offline.mode";
    private static final String GRADLE_BUILD_SCANS = "gradle.build.scans";

    public WorkspaceConfiguration readWorkspaceConfig() {
        IEclipsePreferences preferences = getPreferences();
        String distributionString = preferences.get(GRADLE_DISTRIBUTION, null);
        GradleDistribution distribution = distributionString == null
                ? GradleDistribution.fromBuild()
                : GradleDistributionSerializer.INSTANCE.deserializeFromString(distributionString);
        String gradleUserHomeString = preferences.get(GRADLE_USER_HOME, null);
        File gradleUserHome = gradleUserHomeString == null
                ? null
                : new File(gradleUserHomeString);
        boolean offlineMode = preferences.getBoolean(GRADLE_OFFLINE_MODE, false);
        boolean buildScansEnabled = preferences.getBoolean(GRADLE_BUILD_SCANS, false);

        return new WorkspaceConfiguration(distribution, gradleUserHome, offlineMode, buildScansEnabled);
    }

    public void saveWorkspaceConfiguration(WorkspaceConfiguration config) {
        Preconditions.checkNotNull(config);
        IEclipsePreferences preferences = getPreferences();
        preferences.put(GRADLE_DISTRIBUTION, GradleDistributionSerializer.INSTANCE.serializeToString(config.getGradleDistribution()));
        if (config.getGradleUserHome() == null) {
            preferences.remove(GRADLE_USER_HOME);
        } else {
            preferences.put(GRADLE_USER_HOME, config.getGradleUserHome().getPath());
        }
        preferences.putBoolean(GRADLE_OFFLINE_MODE, config.isOffline());
        preferences.putBoolean(GRADLE_BUILD_SCANS, config.isBuildScansEnabled());
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
