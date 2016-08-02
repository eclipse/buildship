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

import com.google.common.base.Charsets;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import java.io.*;
import java.util.Properties;

/**
 * Unifies how to access preferences in the Eclipse API and in a properties file.
 */
abstract class PreferenceStore {

    private PreferenceStore() {
    }

    /**
     * Reads the preference value. May return {@code null}, if a null-value was stored for the
     * target key.
     *
     * @param key the preference key
     * @return the preference value
     * @throws GradlePluginsRuntimeException if the key is not present in the preference store
     */
    abstract String read(String key);

    /**
     * Reads the preference value. If the preference is not present then the specified default
     * is returned. Returns {@code null} if the stored value is a "null" string or the key is
     * missing from the preference and the supplied default is {@code null} or "null" string.
     *
     * @param key the preference key
     * @param defaultValue the value to return if the key is not present in the preference store
     * @return the preference value
     */
    abstract String read(String key, String defaultValue);

    /**
     * Writes a preference key-value pair. The changes can be persisted by calling {@link #flush()}.
     * The value can be {@code null}.
     *
     * @param key   the preference key
     * @param value the preference value
     */
    abstract void write(String key, String value);

    /**
     * Deletes an entry from the preference store. Does nothing if the key doesn't exist in the
     * store. The changes can be persisted by calling {@link #flush()}.
     *
     * @param key the key to remove from the preference store
     */
    abstract void delete(String key);

    /**
     * Persists changes done on this preference store.
     *
     * @throws GradlePluginsRuntimeException if the operation fails
     */
    abstract void flush();

    static String fromRawValue(String value) {
        return "null".equals(value) ? null : value;
    }

    static String toRawValue(String value) {
        return value == null ? "null" : value;
    }

    /**
     * Creates a new preference store based on Eclipse project-scoped preferences.
     *
     * @param project the target project where to access the preferences
     * @param node    the target node under the project where to access the preferences
     * @return the preference store
     * @throws GradlePluginsRuntimeException if the the preferences can't be loaded
     */
    static PreferenceStore forProjectScope(IProject project, String node) {
        return new ProjectScopeEclipsePreferencesPreferenceStore(project, node);
    }

    /**
     * Creates a new preference store based on a properties file.
     *
     * @param propertiesFile the target properties file
     * @return the preference store
     * @throws GradlePluginsRuntimeException if the the preferences can't be loaded from the
     *                                       properties file location
     */
    static PreferenceStore forPreferenceFile(File propertiesFile) {
        return new PropertiesFilePreferenceStore(propertiesFile);
    }

    /**
     * Preference store backed by project-scoped Eclipse preferences.
     */
    private static final class ProjectScopeEclipsePreferencesPreferenceStore extends PreferenceStore {

        private final IProject project;
        private final String node;
        private final IEclipsePreferences preferences;

        private ProjectScopeEclipsePreferencesPreferenceStore(IProject project, String node) {
            this.project = project;
            this.node = node;
            this.preferences = new ProjectScope(project).getNode(node);
        }

        @Override
        String read(String key) {
            String rawValue = this.preferences.get(key, null);
            if (rawValue == null) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference %s in project %s in node %s.", key, this.project.getName(), this.node));
            } else {
                return fromRawValue(rawValue);
            }
        }

        @Override
        String read(String key, String defaultValue) {
            String rawValue = this.preferences.get(key, defaultValue);
            return fromRawValue(rawValue);
        }

        @Override
        void write(String key, String value) {
            this.preferences.put(key, toRawValue(value));
        }

        @Override
        void delete(String key) {
            this.preferences.remove(key);
        }

        @Override
        void flush() {
            try {
                this.preferences.flush();
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot store preferences in project %s in node %s.", this.project.getName(), this.node), e);
            }
        }

    }

    /**
     * Preference store backed by a properties file.
     */
    private static final class PropertiesFilePreferenceStore extends PreferenceStore {

        private final File propertiesFile;
        private Properties properties;

        private PropertiesFilePreferenceStore(File propertiesFile) {
            this.propertiesFile = propertiesFile;
        }

        private Properties getProperties() {
            if (this.properties == null) {
                loadProperties();
            }
            return this.properties;
        }

        private void loadProperties() {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(this.propertiesFile), Charsets.UTF_8);
                this.properties = new Properties();
                this.properties.load(reader);
            } catch (IOException e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference from file %s", this.propertiesFile.getAbsolutePath()), e);
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

        @Override
        String read(String key) {
            String rawValue = getProperties().getProperty(key, null);
            if (rawValue == null) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference %s from file %s.", key, this.propertiesFile.getAbsolutePath()));
            } else {
                return fromRawValue(rawValue);
            }
        }

        @Override
        String read(String key, String defaultValue) {
            String rawValue = getProperties().getProperty(key, defaultValue);
            return fromRawValue(rawValue);
        }

        @Override
        void write(String key, String value) {
            getProperties().put(key, toRawValue(value));
        }

        @Override
        void delete(String key) {
            getProperties().remove(key);
        }

        @Override
        void flush() {
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(this.propertiesFile), Charsets.UTF_8);
                getProperties().store(writer, null);
            } catch (IOException e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot store preferences in file %s", this.propertiesFile.getAbsolutePath()), e);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }

    }

}
