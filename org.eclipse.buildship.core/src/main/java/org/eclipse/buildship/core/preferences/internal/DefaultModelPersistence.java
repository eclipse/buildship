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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.preferences.ModelPersistence;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.workspace.ProjectMovedEvent;

/**
 * Default implementation for {@link PersistentModel}.
 *
 * @author Donat Csikos
 */
public final class DefaultModelPersistence implements ModelPersistence, EventListener {

    private DefaultModelPersistence() {
    }

    @Override
    public PersistentModel loadModel(IProject project) {
        try {
            return loadProjectPrefsChecked(project);
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private PersistentModel loadProjectPrefsChecked(IProject project) throws IOException {
        File preferencesFile = preferencesFile(project);
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile(project)), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                HashMap<String, String> preferences = Maps.newHashMap();
                for (Object propKey : props.keySet()) {
                    preferences.put(propKey.toString(), props.get(propKey).toString());
                }
                return new DefaultPersistentModel(project, preferences);
            }
        } else {
            return new DefaultPersistentModel(project, Collections.<String, String>emptyMap());
        }
    }

    void persistPrefs(DefaultPersistentModel preferences) {
        try {
            persistPrefsChecked(preferences);
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private void persistPrefsChecked(DefaultPersistentModel preferences) throws IOException {
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
    public void saveModel(PersistentModel model) {
        if (!(model instanceof DefaultPersistentModel)) {
            throw new GradlePluginsRuntimeException("Can't save PersistentModel class " + model.getClass().getName());
        }
        DefaultPersistentModel persistentModel = (DefaultPersistentModel) model;
        persistPrefs(persistentModel);
        persistentModel.getAdded().clear();
        persistentModel.getRemoved().clear();
    }

    @Override
    public void deleteModel(IProject project) {
        preferencesFile(project).delete();
    }

    @Override
    public void onEvent(Event event) {
        try {
            if (event instanceof ProjectMovedEvent) {
                movePreferencesFile((ProjectMovedEvent) event);
            } else if (event instanceof ProjectDeletedEvent) {
                deleteProjectPreferences((ProjectDeletedEvent) event);
            }
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    private void movePreferencesFile(ProjectMovedEvent event) throws IOException {
        File preferencesFile = preferencesFile(event.getPreviousName());
        if (preferencesFile.exists()) {
            Files.move(preferencesFile, preferencesFile(event.getProject().getName()));
        }
    }

    private void deleteProjectPreferences(ProjectDeletedEvent event) {
        deleteModel(event.getProject());
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
