/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.gradle.internal.UncheckedException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.preferences.ProjectPluginStatePreferenceStore;
import org.eclipse.buildship.core.preferences.ProjectPluginStatePreferences;

/**
 * Default implementation for {@link ProjectPluginStatePreferences}.
 *
 * @author Donat Csikos
 */
public final class DefaultProjectPluginStatePreferenceStore implements ProjectPluginStatePreferenceStore, ProjectChangeHandler {

    private final ProjectChangeListener projectChangeListener;

    private DefaultProjectPluginStatePreferenceStore() {
        this.projectChangeListener = new ProjectChangeListener(this);
    }

    private void init() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this.projectChangeListener, IResourceChangeEvent.POST_CHANGE);
    }

    public void close() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.projectChangeListener);
    }

    @Override
    public ProjectPluginStatePreferences loadProjectPrefs(IProject project) {
        try {
            return loadProjectPrefsChecked(project);
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private ProjectPluginStatePreferences loadProjectPrefsChecked(IProject project) throws IOException {
        File preferencesFile = preferencesFile(project);
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile(project)), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                HashMap<String, String> preferences = Maps.newHashMap();
                for (Object propKey : props.keySet()) {
                    preferences.put(propKey.toString(), props.get(propKey).toString());
                }
                return new DefaultProjectPluginStatePreferences(this, project, preferences);
            }
        } else {
            return new DefaultProjectPluginStatePreferences(this, project, Collections.<String, String>emptyMap());
        }
    }

    void persistPrefs(DefaultProjectPluginStatePreferences preferences) {
        try {
            persistPrefsChecked(preferences);
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private void persistPrefsChecked(DefaultProjectPluginStatePreferences preferences) throws IOException {
        Map<String, String> added = preferences.getAdded();
        Set<String> removed = preferences.getRemoved();
        if (!added.isEmpty() || !removed.isEmpty()) {
            File preferencesFile = preferencesFile(preferences.getProject());
            if (!preferencesFile.exists()) {
                Files.createParentDirs(preferencesFile);
                Files.touch(preferencesFile);
            }

            Properties props = new Properties();
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile), Charsets.UTF_8)) {
                props.load(reader);
            }

            for (String key : removed) {
                props.remove(key);
            }
            for (String key : added.keySet()) {
                props.put(key, added.get(key));
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(preferencesFile), Charsets.UTF_8)) {
                props.store(writer, "");
            }
        }
    }

    @Override
    public void projectMoved(IPath from, IProject to) throws IOException {
        String deletedName = from.lastSegment();
        String addedName = to.getName();
        File preferencesFile = preferencesFile(deletedName);
        if (preferencesFile.exists()) {
            Files.move(preferencesFile, preferencesFile(addedName));
        }
    }

    @Override
    public void projectDeleted(IProject project) {
        preferencesFile(project).delete();
    }

    private static File preferencesFile(IProject project) {
        return preferencesFile(project.getName());
    }

    private static File preferencesFile(String projectName) {
        return CorePlugin.getInstance().getStateLocation().append("project-preferences").append(projectName).toFile();
    }

    public static DefaultProjectPluginStatePreferenceStore createNew() {
        DefaultProjectPluginStatePreferenceStore preferencesStore = new DefaultProjectPluginStatePreferenceStore();
        preferencesStore.init();
        return preferencesStore;
    }
}
