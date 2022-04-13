/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseProject;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import org.eclipse.buildship.model.ExtendedEclipseModel;
import org.eclipse.buildship.model.ProjectInGradleConfiguration;
import org.eclipse.buildship.model.SourceSet;
import org.eclipse.buildship.model.TestTask;

class ExtendedEclipseModelBuilder implements ToolingModelBuilder {

    protected final ToolingModelBuilderRegistry registry;

    public ExtendedEclipseModelBuilder(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(ExtendedEclipseModel.class.getName());
    }

    @Override
    public Object buildAll(String modelName, Project modelRoot) {
        ToolingModelBuilder eclipseModelBuilder = this.registry.getBuilder("org.gradle.tooling.model.eclipse.EclipseProject");
        DefaultEclipseProject eclipseProject = (DefaultEclipseProject) eclipseModelBuilder.buildAll(modelName, modelRoot);
        return build(eclipseProject, modelRoot);
    }

    protected DefaultExtendedEclipseModel build(DefaultEclipseProject eclipseProject, Project modelRoot) {
        List<ProjectInGradleConfiguration> projects = new ArrayList<>();
        for (Project project : modelRoot.getRootProject().getAllprojects()) {
            File location = project.getProjectDir();
            Set<SourceSet> sourceSets = collectSourceSets(project);
            Set<TestTask> testTasks = collectTestTasks(project);

            projects.add(new DefaultProjectInGradleConfiguration(location, sourceSets, testTasks));
        }
        return new DefaultExtendedEclipseModel(projects, eclipseProject);
    }

    private Set<SourceSet> collectSourceSets(Project project) {
        Set<SourceSet> result = new LinkedHashSet<>();
        try {
            result.addAll(readSourceSetNamesFromJavaExtension(project));
        } catch (Throwable ignore) {
            result.addAll(readSourceSetNamesFromJavaConvention(project));
        }
        return result;
    }

    private Set<TestTask> collectTestTasks(Project project) {
        Set<TestTask> result = new LinkedHashSet<>();
        TaskCollection<Test> tasks = project.getTasks().withType(Test.class);
        for (Test task : tasks) {
            String path = task.getPath();
            Set<File> testClassesDirs = task.getTestClassesDirs().getFiles();
            result.add(new DefaultTestTask(path, testClassesDirs));
        }
        return result;
    }

    private static Set<SourceSet> readSourceSetNamesFromJavaExtension(Project project) throws Throwable {
        JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);
        return from(javaPluginExtension.getSourceSets());
    }

    @SuppressWarnings("deprecation")
    private static Set<SourceSet> readSourceSetNamesFromJavaConvention(Project project) {
        JavaPluginConvention convention = project.getConvention().findByType(JavaPluginConvention.class);
        if (convention != null) {
            return from(convention.getSourceSets());
        } else {
            return Collections.emptySet();
        }
    }

    private static Set<SourceSet> from(SourceSetContainer sourceSetContainer) {
        Collection<org.gradle.api.tasks.SourceSet> gradleSourceSets = sourceSetContainer.getAsMap().values();
        Set<SourceSet> result = new LinkedHashSet<>(gradleSourceSets.size());
        for (org.gradle.api.tasks.SourceSet gradleSourceSet : gradleSourceSets) {
            result.add(from(gradleSourceSet));
        }
        return result;
    }

    private static SourceSet from(org.gradle.api.tasks.SourceSet gradleSourceSet) {
        return new DefaultSourceSet(gradleSourceSet.getName(), gradleSourceSet.getRuntimeClasspath().getFiles(), gradleSourceSet.getAllSource().getSrcDirs());
    }
}