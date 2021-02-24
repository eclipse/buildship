/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.preferences;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.workspace.ProjectDeletedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectMovedEvent;
import org.eclipse.buildship.core.internal.workspace.WorkbenchShutdownEvent;

/**
 * Default implementation for {@link ModelPersistence}.
 *
 * @author Donat Csikos
 */
public final class DefaultModelPersistence implements ModelPersistence, EventListener {

    private final Cache<String, PersistentModel> modelCache;

    private DefaultModelPersistence() {
        this.modelCache = CacheBuilder.newBuilder().build();
    }

    @Override
    public PersistentModel loadModel(final IProject project) {
        try {
            return this.modelCache.get(project.getName(), new Callable<PersistentModel>() {

                @Override
                public PersistentModel call() throws FileNotFoundException, IOException {
                    return doLoadModel(project);
                }
            });
        } catch (ExecutionException e) {
            CorePlugin.logger().warn("Failed to load model for " + project.getName());
            throw new GradlePluginsRuntimeException(e.getCause());
        }
    }

    @Override
    public void saveModel(PersistentModel model) {
        this.modelCache.put(model.getProject().getName(), model);
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
            throw new RuntimeException(e);
        }
    }

    private void movePreferencesFile(ProjectMovedEvent event) throws IOException {
        String previousName = event.getPreviousName();
        for (String cached : this.modelCache.asMap().keySet()) {
            if (cached.equals(previousName)) {
                PersistentModel model = this.modelCache.getIfPresent(cached);
                // Don't copy absent model as it references the old project
                // https://github.com/eclipse/buildship/issues/936
                if (model.isPresent()) {
                    this.modelCache.put(event.getProject().getName(), model);
                }
                this.modelCache.invalidate(cached);
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

    private static PersistentModel doLoadModel(IProject project) throws IOException, FileNotFoundException {
        String projectName = project.getName();
        File preferencesFile = preferencesFile(projectName);
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile(projectName)), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                return PersistentModelConverter.toModel(project, props);
            }
        } else {
            return new AbsentPersistentModel(project);
        }
    }

    private void persistAllProjectPrefs() {
        Map<String, PersistentModel> modelCacheMap = this.modelCache.asMap();
        for (Entry<String, PersistentModel> entry : modelCacheMap.entrySet()) {
            PersistentModel model = entry.getValue();
            if (model.isPresent()) {
                persistPrefs(entry.getKey(), model);
            }
        }
    }

    private static void persistPrefs(String projectName, PersistentModel model) {
        try {
            persistPrefsChecked(projectName, model);
        } catch (IOException e) {
            CorePlugin.logger().warn("Can't save persistent model for project " + projectName, e);
        }
    }

    private static void persistPrefsChecked(String projectName, PersistentModel model) throws IOException {
        File preferencesFile = preferencesFile(projectName);

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
                        try {
                            loadModel(project);
                        } catch (Exception e) {
                            CorePlugin.logger().warn("Can't load persistent model for project " + project.getName(), e);
                        }
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
