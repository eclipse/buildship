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

package org.eclipse.buildship.core.internal.configuration.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Unifies how to access preferences in the Eclipse API and in a properties file.
 */
abstract class PreferenceStore {

    private PreferenceStore() {
    }

    /**
     * Reads a string. If the key was not present, the default value is returned.
     *
     * @param key the preference key
     * @param defaultValue the default value to return if the key is not present
     * @return the preference value
     */
    abstract String readString(String key, String defaultValue);

    /**
     * Reads a boolean value. If the key was not present, the default value is returned.
     *
     * @param key the preference key
     * @param defaultValue the default value to return if the key is not present
     * @return the preference value
     */
    abstract boolean readBoolean(String key, boolean defaultValue);

    /**
     * Writes a preference key-value pair. The changes can be persisted by calling {@link #flush()}.
     * If the value is {@code null}, the preference is removed.
     *
     * @param key the preference key
     * @param value the preference value
     */
    abstract void write(String key, String value);

    /**
     * Writes a preference key-value pair. The changes can be persisted by calling {@link #flush()}.
     *
     * @param key the preference key
     * @param value the preference value
     */
    abstract void writeBoolean(String key, boolean value);

    /**
     * Deletes an entry from the preference store. Does nothing if the key doesn't exist in the
     * store. The changes can be persisted by calling {@link #flush()}.
     *
     * @param key the key to remove from the preference store
     */
    void delete(String key) {
        write(key, null);
    }

    /**
     * Persists changes done on this preference store.
     *
     * @throws GradlePluginsRuntimeException if the operation fails
     */
    abstract void flush();

    /**
     * Creates a new preference store based on Eclipse project-scoped preferences.
     *
     * @param project the target project where to access the preferences
     * @param node the target node under the project where to access the preferences
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
     *             properties file location
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
        String readString(String key, String defaultValue) {
            return this.preferences.get(key, defaultValue);
        }

        @Override
        boolean readBoolean(String key, boolean defaultValue) {
            return this.preferences.getBoolean(key, defaultValue);
        }

        @Override
        void write(String key, String value) {
            if (value == null) {
                this.preferences.remove(key);
            } else {
                this.preferences.put(key, value);
            }
        }

        @Override
        void writeBoolean(String key, boolean value) {
            this.preferences.putBoolean(key, value);
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
                this.properties = new SortedProperties();
                if (this.propertiesFile.exists()) {
                    reader = new InputStreamReader(new FileInputStream(this.propertiesFile), Charsets.UTF_8);
                    this.properties.load(reader);
                }
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
        String readString(String key, String defaultValue) {
            return getProperties().getProperty(key, defaultValue);
        }

        @Override
        boolean readBoolean(String key, boolean defaultValue) {
            String value = getProperties().getProperty(key, null);
            return value == null ? defaultValue : Boolean.parseBoolean(value);
        }

        @Override
        void write(String key, String value) {
            if (value == null) {
                getProperties().remove(key);
            } else {
                getProperties().put(key, value);
            }
        }

        @Override
        void writeBoolean(String key, boolean value) {
            getProperties().put(key, String.valueOf(value));
        }

        @Override
        void flush() {
            this.properties.put("eclipse.preferences.version", "1");
            OutputStream output = null;
            try {
                if (!this.propertiesFile.exists()) {
                    this.propertiesFile.getParentFile().mkdirs();
                    Files.touch(this.propertiesFile);
                }

                output = new FileOutputStream(this.propertiesFile);

                output.write(removeTimestampFromTable(this.properties).getBytes("UTF-8")); //$NON-NLS-1$
                output.flush();
            } catch (IOException e) {
                throw new GradlePluginsRuntimeException(String.format("Cannot store preferences in file %s", this.propertiesFile.getAbsolutePath()), e);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        /*
         * From org.eclipse.core.internal.preference.EclipsePreferences.
         */
        protected static String removeTimestampFromTable(Properties properties) throws IOException {
            // store the properties in a string and then skip the first line (date/timestamp)
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                properties.store(output, null);
            } finally {
                output.close();
            }
            String string = output.toString("UTF-8");
            String separator = System.getProperty("line.separator");
            return string.substring(string.indexOf(separator) + separator.length());
        }
    }

    /**
     * Properties with sorted key set.
     * <p/>
     * Taken from org.eclipse.core.internal.preferences; the e42 defines this class at a different
     * location.
     */
    public class SortedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Enumeration<Object> keys() {
            TreeSet<Object> set = new TreeSet<>();
            for (Enumeration<?> e = super.keys(); e.hasMoreElements();) {
                set.add(e.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override
        public Set<Entry<Object, Object>> entrySet() {
            TreeSet<Entry<Object, Object>> set = new TreeSet<>(new Comparator<Entry<Object, Object>>() {

                @Override
                public int compare(Entry<Object, Object> e1, Entry<Object, Object> e2) {
                    String s1 = (String) e1.getKey();
                    String s2 = (String) e2.getKey();
                    return s1.compareTo(s2);
                }
            });
            for (Iterator<Entry<Object, Object>> i = super.entrySet().iterator(); i.hasNext();) {
                set.add(i.next());
            }
            return set;
        }
    }

}
