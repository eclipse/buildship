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
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.gradle.BuildRunner;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;

/**
 * This class is used to create a Gradle project. Internally gradle init is called on a temp
 * directory and the build.gradle and settings.gradle is manipulated according to the given
 * parameters.
 */
public class GradleInitializer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private GradleInitializer() {
    }

    public static File getInitializedGradleFiles(IProgressMonitor monitor, GradleRunConfigurationAttributes configurationAttributes, String rootProjectName,
            List<SourceSet> sourceSets, List<String> gradlePlugins, List<String> repositories, List<String> fileSystemClassPaths) throws IOException {
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
                Files.write("rootProject.name = '" + rootProjectName + "'", file, Charsets.UTF_8);
            } else if (file.getName().startsWith("build")) {
                String gradleBuildFileContents = getBuildFileContents(gradlePlugins, repositories, sourceSets, fileSystemClassPaths);
                Files.write(gradleBuildFileContents, file, Charsets.UTF_8);
            }
        }

        return gradleInitTempDir;
    }

    private static String getBuildFileContents(List<String> gradlePlugins, List<String> repositories, List<SourceSet> sourceSets, List<String> fileSystemClassPaths)
            throws IOException {
        StringBuilder buffer = new StringBuilder();
        addGradlePlugins(buffer, gradlePlugins);
        buffer.append(LINE_SEPARATOR);
        addRepositories(buffer, repositories);
        buffer.append(LINE_SEPARATOR);
        addSourceSets(buffer, sourceSets);
        buffer.append(LINE_SEPARATOR);
        addDependencies(buffer, fileSystemClassPaths);
        return buffer.toString();
    }

    private static void addSourceSets(Appendable appendable, List<SourceSet> sourceSets) throws IOException {
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

    private static void addGradlePlugins(Appendable appendable, List<String> gradlePlugins) throws IOException {
        for (String gradlePlugin : gradlePlugins) {
            appendable.append("apply plugin: '");
            appendable.append(gradlePlugin);
            appendable.append("'");
            appendable.append(LINE_SEPARATOR);
        }
    }

    private static void addRepositories(Appendable appendable, List<String> repositories) throws IOException {
        if (repositories.isEmpty()) {
            return;
        }
        appendable.append("repositories {");
        appendable.append(LINE_SEPARATOR);

        for (String repository : repositories) {
            appendable.append(repository);
            appendable.append(LINE_SEPARATOR);
        }

        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
    }

    private static void addDependencies(Appendable appendable, List<String> fileSystemClassPaths) throws IOException {
        if(fileSystemClassPaths.isEmpty()) {
            return;
        }
        appendable.append("dependencies {");
        appendable.append(LINE_SEPARATOR);
        for (String classPath : fileSystemClassPaths) {
            appendable.append("compile files('");
            appendable.append(classPath);
            appendable.append("')");
            appendable.append(LINE_SEPARATOR);
        }
        appendable.append("}");
        appendable.append(LINE_SEPARATOR);
    }

}
