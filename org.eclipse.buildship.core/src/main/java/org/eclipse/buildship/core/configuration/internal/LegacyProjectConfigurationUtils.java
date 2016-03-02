/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Cleans up project artifacts created by previous versions of Buildship.
 */
final class LegacyProjectConfigurationUtils {

    private static final String GRADLE_PREFERENCES_LOCATION = ".settings/gradle.prefs";
    private static final String GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION = "gradle";

    private LegacyProjectConfigurationUtils() {
    }

    public static void cleanup(IProject project) {
        Preconditions.checkNotNull(project);
        Preconditions.checkArgument(project.isAccessible());

        File gradlePrefsFile = new File(project.getLocation().toFile(), GRADLE_PREFERENCES_LOCATION);
        if (gradlePrefsFile.exists()) {
            try {
                String gradlePrefs = getContents(gradlePrefsFile);
                savePreferences(project, gradlePrefs);
                ensureNoProjectPreferencesLoadedFrom(project);
                deleteLegacyFile(gradlePrefsFile);
            } catch (Exception e) {
                CorePlugin.logger().warn(String.format("Cannot clean up legacy project configuration on project %s", project.getName()), e);
            }
        }
    }

    private static String getContents(File file) throws IOException {
        InputStreamReader reader = null;
        try {
            return CharStreams.toString(reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static void savePreferences(IProject project, String projectPreferences) throws BackingStoreException {
        ProjectScope scope = new ProjectScope(project);
        IEclipsePreferences preferences = scope.getNode(CorePlugin.PLUGIN_ID);
        preferences.put(ProjectConfigurationPersistence.GRADLE_PROJECT_CONFIGURATION, projectPreferences);
        preferences.flush();
    }

    private static void ensureNoProjectPreferencesLoadedFrom(IProject project) throws BackingStoreException {
        // The ${project_name}/.settings/gradle.prefs file is automatically loaded as project
        // preferences by the core runtime since the fie extension is '.prefs'. If the preferences
        // are loaded, then deleting the prefs file results in a BackingStoreException.
        ProjectScope projectScope = new ProjectScope(project);
        IEclipsePreferences node = projectScope.getNode(GRADLE_PREFERENCES_FILE_NAME_WITHOUT_EXTENSION);
        if (node != null) {
            node.removeNode();
        }
    }

    private static void deleteLegacyFile(File gradlePrefsFile) throws IOException {
        boolean result = gradlePrefsFile.delete();
        if (!result) {
            throw new IOException(String.format("Cannot delete old preference file %s", gradlePrefsFile.getAbsolutePath()));
        }
    }

}
