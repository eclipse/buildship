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
import java.io.FileNotFoundException;
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

import org.gradle.internal.UncheckedException;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.preferences.ModelPersistence;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.workspace.ProjectMovedEvent;
import org.eclipse.buildship.core.workspace.WorkbenchShutdownEvent;

/**
 * Default implementation for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
public final class DefaultModelPersistence implements ModelPersistence, EventListener {

    // Cache holding (project name - persistent model property) pairs.
    private final LoadingCache<String, Map<String, String>> modelCache;

    private DefaultModelPersistence() {
        this.modelCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {

            @Override
            public Map<String, String> load(String projectName) throws Exception {
                return loadPrefs(projectName);
            }
        });
    }

    @Override
    public PersistentModel loadModel(IProject project) {
        return new DefaultPersistentModel(project, this.modelCache.getUnchecked(project.getName()));
    }

    @Override
    public void saveModel(PersistentModel model) {
        if (!(model instanceof DefaultPersistentModel)) {
            throw new GradlePluginsRuntimeException("Can't save PersistentModel class " + model.getClass().getName());
        }
        DefaultPersistentModel persistentModel = (DefaultPersistentModel) model;
        this.modelCache.put(persistentModel.getProject().getName(), persistentModel.getEntries());
    }

    @Override
    public void deleteModel(IProject project) {
        preferencesFile(project).delete();
        this.modelCache.invalidate(project.getName());
    }

    @Override
    public void onEvent(Event event) {
        try {
            if (event instanceof ProjectMovedEvent) {
                movePreferencesFile((ProjectMovedEvent) event);
            } else if (event instanceof ProjectDeletedEvent) {
                deleteProjectPreferences((ProjectDeletedEvent) event);
            } else if (event instanceof WorkbenchShutdownEvent) {
                persistAllProjectPrefs();
            }
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private void movePreferencesFile(ProjectMovedEvent event) throws IOException {
        Map<String, String> entries = this.modelCache.getUnchecked(event.getPreviousName());
        this.modelCache.invalidate(event.getPreviousName());
        this.modelCache.put(event.getProject().getName(), entries);

        File preferencesFile = preferencesFile(event.getPreviousName());
        if (preferencesFile.exists()) {
            Files.move(preferencesFile, preferencesFile(event.getProject().getName()));
        }
    }

    private void deleteProjectPreferences(ProjectDeletedEvent event) {
        this.modelCache.invalidate(event.getProject().getName());
        deleteModel(event.getProject());
    }

    private static Map<String, String> loadPrefs(String projectName) throws IOException, FileNotFoundException {
        File preferencesFile = preferencesFile(projectName);
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile(projectName)), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                HashMap<String, String> preferences = Maps.newHashMap();
                for (Object propKey : props.keySet()) {
                    preferences.put(propKey.toString(), props.get(propKey).toString());
                }
                return preferences;
            }
        } else {
            return Collections.emptyMap();
        }
    }

    private void persistAllProjectPrefs() {
        Map<String, Map<String, String>> modelCacheMap = this.modelCache.asMap();
        for (String projectName : modelCacheMap.keySet()) {
            persistPrefs(projectName, modelCacheMap.get(projectName));
        }
    }

    private static void persistPrefs(String projectName, Map<String, String> preferences) {
        try {
            persistPrefsChecked(projectName, preferences);
        } catch (IOException e) {
            CorePlugin.logger().warn("Can't save persistent model for project " + projectName, e);
        }
    }

    private static void persistPrefsChecked(String projectName, Map<String, String> preferences) throws IOException {
        File preferencesFile = preferencesFile(projectName);

        if (!preferencesFile.exists()) {
            Files.createParentDirs(preferencesFile);
            Files.touch(preferencesFile);
        }

        Properties props = new Properties();
        for (String key : preferences.keySet()) {
            props.setProperty(key, preferences.get(key));
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(preferencesFile), Charsets.UTF_8)) {
            props.store(writer, "");
        }
    }

    private static File preferencesFile(IProject project) {
        return preferencesFile(project.getName());
    }

    private static File preferencesFile(String projectName) {
        return CorePlugin.getInstance().getStateLocation().append("project-preferences").append(projectName).toFile();
    }

    public static DefaultModelPersistence createAndRegister() {
        DefaultModelPersistence persistence = new DefaultModelPersistence();
        CorePlugin.listenerRegistry().addEventListener(persistence);
        return persistence;
    }

    public void close() {
        CorePlugin.listenerRegistry().removeEventListener(this);
    }
}
