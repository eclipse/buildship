/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.project.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.buildship.core.gradle.BuildRunner;
import org.eclipse.buildship.core.gradle.model.Dependency;
import org.eclipse.buildship.core.gradle.model.GradleModel;
import org.eclipse.buildship.core.gradle.model.Plugin;
import org.eclipse.buildship.core.gradle.model.Repository;
import org.eclipse.buildship.core.gradle.model.SourceSet;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * This class is used to create a Gradle project. Internally gradle init is called on a temp
 * directory and the build.gradle and settings.gradle is manipulated according to the given
 * parameters.
 */
public class GradleInitializer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private GradleInitializer() {
    }

    public static File getInitializedGradleFiles(IProgressMonitor monitor, GradleRunConfigurationAttributes configurationAttributes, GradleModel gradleModel) throws IOException {
        // do a simple gradle init in a temp directory
        File gradleInitTempDir = Files.createTempDir();
        gradleInitTempDir.deleteOnExit();
        ImmutableList<String> tasks = ImmutableList.<String> of("init");
        BuildRunner.runGradleBuild(monitor, tasks, gradleInitTempDir, configurationAttributes.getGradleDistribution(), configurationAttributes.getGradleUserHome(),
                configurationAttributes.getJavaHome(), configurationAttributes.getJvmArguments(), configurationAttributes.getArguments());

        File[] listFiles = gradleInitTempDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".gradle");
            }
        });
        for (File file : listFiles) {
            if (file.getName().startsWith("settings")) {
                Files.write("rootProject.name = '" + gradleModel.getRootProjectName() + "'", file, Charsets.UTF_8);
            } else if (file.getName().startsWith("build")) {
                String gradleBuildFileContents = getBuildFileContents(gradleModel.getPlugins(), gradleModel.getRepositories(), gradleModel.getSourceSets(), gradleModel.getDependencies());
                Files.write(gradleBuildFileContents, file, Charsets.UTF_8);
            }
        }

        return gradleInitTempDir;
    }

    private static String getBuildFileContents(Collection<Plugin> plugins, Collection<Repository> repositories,
			Collection<SourceSet> sourceSets, Collection<Dependency> dependencies) throws IOException {
        StringBuilder sb = new StringBuilder();
        addGradlePlugins(sb, plugins);
        sb.append(LINE_SEPARATOR);
        addRepositories(sb, repositories);
        sb.append(LINE_SEPARATOR);
        addSourceSets(sb, sourceSets);
        sb.append(LINE_SEPARATOR);
        addDependencies(sb, dependencies);
        return sb.toString();
	}


    private static void addSourceSets(Appendable appendable, Collection<SourceSet> sourceSets) throws IOException {
        if(sourceSets.isEmpty()) {
            return;
        }
        appendable.append("sourceSets {");
        appendable.append(LINE_SEPARATOR);
        appendable.append("main {");
        for (SourceSet sourceSet : sourceSets) {
            appendable.append(LINE_SEPARATOR);
            String name = sourceSet.getName();
            appendable.append(name);
            appendable.append("{");
            appendable.append(LINE_SEPARATOR);
            appendable.append("srcDir '");
            appendable.append(sourceSet.getPath());
            appendable.append("'");
            appendable.append(LINE_SEPARATOR);
            appendable.append("}");
        }
        appendable.append(LINE_SEPARATOR);
        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
    }

    private static void addGradlePlugins(Appendable appendable, Collection<Plugin> gradlePlugins) throws IOException {
        for (Plugin gradlePlugin : gradlePlugins) {
            appendable.append("apply plugin: '");
            appendable.append(gradlePlugin.getName());
            appendable.append("'");
            appendable.append(LINE_SEPARATOR);
        }
    }

    private static void addRepositories(Appendable appendable, Collection<Repository> repositories) throws IOException {
        if (repositories.isEmpty()) {
            return;
        }
        appendable.append("repositories {");
        appendable.append(LINE_SEPARATOR);

        for (Repository repository : repositories) {
            appendable.append(repository.getRepository());
            appendable.append(LINE_SEPARATOR);
        }

        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
    }

    private static void addDependencies(Appendable appendable, Collection<Dependency> dependencies) throws IOException {
        if(dependencies.isEmpty()) {
            return;
        }
        appendable.append("dependencies {");
        appendable.append(LINE_SEPARATOR);
        for (Dependency dependency : dependencies) {
            appendable.append("compile files('");
            appendable.append(dependency.getDependencyString());
            appendable.append("')");
            appendable.append(LINE_SEPARATOR);
        }
        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
    }

}
