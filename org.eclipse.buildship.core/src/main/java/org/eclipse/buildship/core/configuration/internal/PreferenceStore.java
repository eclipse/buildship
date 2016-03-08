/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.eclipse.buildship.core.configuration.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.google.common.base.Charsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

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
    public abstract String read(String key);

    /**
     * Writes a preference key-value pair. The changes can be persisted by calling {@link #flush()}.
     * The value can be {@code null}.
     *
     * @param key the preference key
     * @param value the preference value
     */
    public abstract void write(String key, String value);

    /**
     * Deletes an entry from the preference store. Does nothing if the key doesn't exist in the
     * store. The changes can be persisted by calling {@link #flush()}.
     *
     * @param key the key to remove from the preference store.
     */
    public abstract void delete(String key);

    /**
     * Persists changes done on this preference store.
     *
     * @throws GradlePluginsRuntimeException if the operation fails
     */
    public abstract void flush();

    protected static String fromRawValue(String value) {
        if ("null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    protected static String toRawValue(String value) {
        if (value == null) {
            return "null";
        } else {
            return value;
        }
    }

    /**
     * Creates new preference store based on Eclipse project-scoped preferences.
     *
     * @param project the target project where to access the preferences
     * @param node the target node under the project where to access the preferences
     * @return the preference store
     * @throws GradlePluginsRuntimeException if the the preferences can't be loaded
     */
    public static PreferenceStore forProjectScope(IProject project, String node) {
        return new ProjectScopeEclipsPreferencesPreferenceStore(project, node);
    }

    /**
     * Creates new preference store based on a properties file
     *
     * @param propertiesFile the target properties file
     * @return the preference store
     * @throws GradlePluginsRuntimeException if the the preferences can't be loaded from the
     *             properties file location
     */
    public static PreferenceStore forPreferenceFile(File propertiesFile) {
        return new PropertiesFilePreferenceStore(propertiesFile);
    }

    private static final class ProjectScopeEclipsPreferencesPreferenceStore extends PreferenceStore {

        private final IProject project;
        private final String node;
        private final IEclipsePreferences preferences;

        private ProjectScopeEclipsPreferencesPreferenceStore(IProject project, String node) {
            try {
                this.project = project;
                this.node = node;
                this.preferences = new ProjectScope(project).getNode(node);
                this.preferences.keys();
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference in project %s in node %s", this.project.getName(), this.node), e);
            }
        }

        @Override
        public String read(String key) {
            String rawValue = this.preferences.get(key, null);
            if (rawValue == null) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference %s in project %s in node %s", key, this.project.getName(), this.node));
            } else {
                return fromRawValue(rawValue);
            }
        }

        @Override
        public void write(String key, String value) {
            this.preferences.put(key, toRawValue(value));
        }

        @Override
        public void delete(String key) {
            this.preferences.remove(key);
        }

        @Override
        public void flush() {
            try {
                this.preferences.flush();
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot store preferences %s in project %s in node %s", this.project.getName(), this.node), e);
            }
        }

    }

    private static final class PropertiesFilePreferenceStore extends PreferenceStore {

        private final Properties properties;
        private final File propertiesFile;

        private PropertiesFilePreferenceStore(File propertiesFile) {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(propertiesFile), Charsets.UTF_8);
                this.propertiesFile = propertiesFile;
                this.properties = new Properties();
                this.properties.load(reader);
            } catch (Exception e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot load preference from file %s", propertiesFile.getAbsolutePath()), e);
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
        public String read(String key) {
            String rawValue = this.properties.getProperty(key, null);
            if (rawValue == null) {
                throw new GradlePluginsRuntimeException(String.format("Cannot read preference %s from file %s", key, this.propertiesFile.getAbsolutePath()));
            } else {
                return fromRawValue(rawValue);
            }
        }

        @Override
        public void write(String key, String value) {
            this.properties.put(key, toRawValue(value));
        }

        @Override
        public void delete(String key) {
            this.properties.remove(key);
        }

        @Override
        public void flush() {
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(this.propertiesFile), Charsets.UTF_8);
                this.properties.store(writer, null);
            } catch (Exception e) {
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
