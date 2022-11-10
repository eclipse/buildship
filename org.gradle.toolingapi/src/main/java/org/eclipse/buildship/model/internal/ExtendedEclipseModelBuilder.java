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
import java.util.Collections;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseProject;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import org.eclipse.buildship.model.ExtendedEclipseModel;

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
        List<DefaultProject> projects = new ArrayList<>();
        for (Project project : modelRoot.getRootProject().getAllprojects()) {
            // location
            File location = project.getProjectDir();
            // source sets
            ArrayList<String> sourceSetNames = new ArrayList<>();
            try {
                sourceSetNames.addAll(readSourceSetNamesFromJavaExtension(project));
            } catch (Throwable ignore) {
                sourceSetNames.addAll(readSourceSetNamesFromJavaConvention(project));
            }
            // encoding // TODO this belongs to the source sets
            DefaultCompileJavaTaskConfiguration compileJavaTaskConfiguration = null;
            Task compileJavaTask = project.getTasks().findByName("compileJava");
            if (compileJavaTask != null && compileJavaTask instanceof JavaCompile) {
                compileJavaTaskConfiguration = new DefaultCompileJavaTaskConfiguration(((JavaCompile)compileJavaTask).getOptions().getEncoding());
            }
            projects.add(new DefaultProject(location, sourceSetNames, compileJavaTaskConfiguration));
        }

        return new DefaultExtendedEclipseModel(projects, eclipseProject);
    }

    private List<String> readSourceSetNamesFromJavaExtension(Project project) throws Throwable {
        JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);
        return new ArrayList<>(javaPluginExtension.getSourceSets().getAsMap().keySet());
    }

    @SuppressWarnings("deprecation")
    private List<String> readSourceSetNamesFromJavaConvention(Project project) {
        JavaPluginConvention convention = project.getConvention().findByType(JavaPluginConvention.class);
        if (convention != null) {
            SourceSetContainer sourceSets = convention.getSourceSets();
            return new ArrayList<>(sourceSets.getAsMap().keySet());
        } else {
            return Collections.emptyList();
        }
    }
}