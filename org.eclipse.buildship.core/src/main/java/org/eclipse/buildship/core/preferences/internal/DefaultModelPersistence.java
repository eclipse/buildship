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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.gradle.api.UncheckedIOException;
import org.gradle.internal.UncheckedException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.preferences.ModelPersistence;
import org.eclipse.buildship.core.preferences.PersistentModel;
import org.eclipse.buildship.core.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.workspace.ProjectMovedEvent;
import org.eclipse.buildship.core.workspace.WorkbenchShutdownEvent;

/**
 * Default implementation for {@link MutablePersistentModel}.
 *
 * @author Donat Csikos
 */
public final class DefaultModelPersistence implements ModelPersistence, EventListener {

    private final Map<IProject, PersistentModel> cache;

    private DefaultModelPersistence() {
        this.cache = Maps.newConcurrentMap();
    }

    @Override
    public PersistentModel loadModel(IProject project) {
        PersistentModel model = this.cache.get(project);
        if (model == null) {
            model = readModel(project);
            if (model != null) {
                this.cache.put(project, model);
            }
        }
        return model;
    }

    @Override
    public void saveModel(PersistentModel model) {
        this.cache.put(model.getProject(), model);
    }

    @Override
    public void deleteModel(IProject project) {
        this.cache.remove(project);
        preferencesFile(project).delete();
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
        String previousName = event.getPreviousName();
        for (IProject project : this.cache.keySet()) {
            if (project.getName().equals(previousName)) {
                PersistentModel model = this.cache.get(project);
                this.cache.put(event.getProject(), model);
                this.cache.remove(project);
            }
        }

        File preferencesFile = preferencesFile(event.getPreviousName());
        if (preferencesFile.exists()) {
            Files.move(preferencesFile, preferencesFile(event.getProject().getName()));
        }
    }

    private void deleteProjectPreferences(ProjectDeletedEvent event) {
        deleteModel(event.getProject());
    }

    private static PersistentModel readModel(IProject project) {
        try {
            return readModelChecked(project);
        } catch (Exception e) {
            throw new UncheckedIOException(e);
        }
    }

    private static PersistentModel readModelChecked(IProject project) throws FileNotFoundException, IOException {
        String projectName = project.getName();
        File preferencesFile = preferencesFile(projectName);
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile(projectName)), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                return PersistentModelConverter.toModel(project, props);
            }
        } else {
            return null;
        }
    }

    private void persistAllProjectPrefs() {
        for (Entry<IProject, PersistentModel> entry : this.cache.entrySet()) {
            persistPrefs(entry.getKey(), entry.getValue());
        }
    }

    private static void persistPrefs(IProject project, PersistentModel model) {
        try {
            persistPrefsChecked(project, model);
        } catch (IOException e) {
            CorePlugin.logger().warn("Can't save persistent model for project " + project.getName(), e);
        }
    }

    private static void persistPrefsChecked(IProject project, PersistentModel model) throws IOException {
        File preferencesFile = preferencesFile(project);

        if (!preferencesFile.exists()) {
            Files.createParentDirs(preferencesFile);
            Files.touch(preferencesFile);
        }

        Properties props = PersistentModelConverter.toProperties(model);

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
        persistence.prefetchCacheAsync();
        return persistence;
    }

    private void prefetchCacheAsync() {
        Job job = new Job("Load persistent model for all projects") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for (IProject project : CorePlugin.workspaceOperations().getAllProjects()) {
                    if (GradleProjectNature.isPresentOn(project)) {
                        loadModel(project);
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    public void close() {
        CorePlugin.listenerRegistry().removeEventListener(this);
    }
}
