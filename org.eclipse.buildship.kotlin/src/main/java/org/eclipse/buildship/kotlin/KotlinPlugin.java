/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle.
 */
public class KotlinPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.buildship.kotlin"; //$NON-NLS-1$

    private static KotlinPlugin plugin;
    private List<String> templateClasspath;

    public KotlinPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static KotlinPlugin getInstance() {
        return plugin;
    }

    public static List<String> templateClasspath() {
        return getInstance().getTemplateClasspath();
    }

    private List<String> getTemplateClasspath() {
        if (this.templateClasspath == null) {
            this.templateClasspath = loadTemplateClasspath();
        }
        return this.templateClasspath;
    }

    private static List<String> loadTemplateClasspath() {
        List<String> classpath = Lists.newArrayList(templateClasspathFor(Files.createTempDir()));
        // classes from this plugin after installation
        appendPluginResourceIfExists(classpath, "/");
        // java classes from this plugin for development time
        appendPluginResourceIfExists(classpath, "/bin");
        // Kotlin classes can't be loaded from the IDE as they're compiled into memory
        // therefore we can only load them from the Gradle build folder
        appendPluginResourceIfExists(classpath, "/build/kotlin-classes/main");
        return classpath;
    }

    private static List<String> templateClasspathFor(File projectDir) {
        ProjectConnection connection = null;
        try {
            connection = GradleConnector.newConnector()
                    .forProjectDirectory(projectDir)
                    .useDistribution(new URI("https://repo.gradle.org/gradle/dist-snapshots/gradle-script-kotlin-3.3-20161205200654+0000-all.zip"))
                    .connect();
            KotlinBuildScriptModel model = connection.getModel(KotlinBuildScriptModel.class);
            List<String> classpath = Lists.newArrayList();
            for (File entry : model.getClassPath()) {
                // an incompatible version of Groovy is already used in the compiler
                if (!entry.getName().startsWith("groovy")) {
                    classpath.add(entry.getAbsolutePath());
                }
            }
            return classpath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void appendPluginResourceIfExists(List<String> classpath, String path) {
        try {
            Bundle pluginBundle = Platform.getBundle(PLUGIN_ID);
            URL entry = pluginBundle.getEntry(path);
            if (entry != null) {
                classpath.add(FileLocator.toFileURL(entry).getFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
