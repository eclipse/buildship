/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.preferences;

import static com.google.common.base.Optional.absent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
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

    private final LoadingCache<IProject, PersistentModel> modelCache;
    private final Object lock = new Object();

    private DefaultModelPersistence() {
        this.modelCache = CacheBuilder.newBuilder().build(new CacheLoader<IProject, PersistentModel>() {

            @Override
            public PersistentModel load(IProject project) throws Exception {
                return doLoadModel(project);
            }
        });
    }

    @Override
    public PersistentModel loadModel(IProject project) {
        return this.modelCache.getUnchecked(project);
    }

    @Override
    public void saveModel(PersistentModel model) {
        this.modelCache.put(model.getProject(), model);
    }

    @Override
    public void deleteModel(IProject project) {
        preferencesFile(project).delete();
        this.modelCache.invalidate(project);
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
        for (IProject cached : this.modelCache.asMap().keySet()) {
            if (cached.getName().equals(previousName)) {
                PersistentModel model = this.modelCache.getUnchecked(cached);
                // Don't copy absent model as it references the old project
                // https://github.com/eclipse/buildship/issues/936
                if (model.isPresent()) {
                    this.modelCache.put(event.getProject(), model);
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

    private Optional<Properties> loadPreferencesForProject(IProject project) throws IOException {
        synchronized (DefaultModelPersistence.this.lock) {
            File preferencesFile = preferencesFile(project.getName());
            if (preferencesFile.exists()) {
                try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile), Charsets.UTF_8)) {
                    Properties props = new Properties();
                    props.load(reader);
                    return Optional.of(props);
                }
            }
            return absent();
        }
    }

    private PersistentModel doLoadModel(IProject project) throws IOException {
        return loadPreferencesForProject(project).transform(props -> PersistentModelConverter.toModel(project, props)).or(() -> new AbsentPersistentModel(project));

    }

    private void persistAllProjectPrefs() {
        Map<IProject, PersistentModel> modelCacheMap = this.modelCache.asMap();
        for (Entry<IProject, PersistentModel> entry : modelCacheMap.entrySet()) {
            PersistentModel model = entry.getValue();
            if (model.isPresent()) {
                persistPrefs(entry.getKey(), model);
            }
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
                        try {
                            DefaultModelPersistence.this.modelCache.get(project);
                        } catch (ExecutionException e) {
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
