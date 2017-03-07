/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jetbrains.kotlin.core.model.ScriptTemplateProviderEx;

import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IFile;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Contributes the Gradle Kotlin Script template to the Kotlin Eclipse integration.
 *
 * @author Donat Csikos
 */
public class GradleKotlinScriptTemplateProvider implements ScriptTemplateProviderEx {

    // properties names defined in gradle-script-kotlin
    private static final String GSK_PROJECT_ROOT = "projectRoot";
    private static final String GSK_GRADLE_HOME = "gradleHome";
    private static final String GSK_JAVA_HOME = "gradleJavaHome";
    private static final String GSK_JVM_OPTIONS = "gradleJvmOptions";
    // properties names not yet used by gradle-script-kotlin
    // see https://github.com/gradle/gradle-script-kotlin/issues/265
    private static final String NON_GSK_GRADLE_USER_HOME = "gradleUserHome";
    private static final String NON_GSK_OPTIONS = "gradleOptions";

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> getTemplateClasspath(Map<String, ? extends Object> environment) {
        File projectDir = (File) environment.get(GSK_PROJECT_ROOT);
        File gradleDist = (File) environment.get(GSK_GRADLE_HOME);
        File gradleUserHome = (File) environment.get(NON_GSK_GRADLE_USER_HOME);
        File javaHome = (File)environment.get(GSK_JAVA_HOME);
        List<String> arguments = (List<String>) environment.get(NON_GSK_OPTIONS);
        List<String> jvmArguments = (List<String>) environment.get(GSK_JVM_OPTIONS);

        KotlinBuildScriptModel model = execute(projectDir, gradleDist, javaHome, gradleUserHome, arguments, jvmArguments);

        List<String> result = Lists.newArrayList();
        for (File f : model.getClassPath()) {
            result.add(f.getAbsolutePath());
        }
        return result;
    }

    private static KotlinBuildScriptModel execute(File projectDir, File gradleDist, File gradleUserHome, File javaHome, List<String> arguments, List<String> jvmArguments) {
        List<String> effectiveJvmArguments = Lists.newArrayList("-Dorg.gradle.script.lang.kotlin.provider.mode=classpath"); // from KotlinScriptPluginFactory
        effectiveJvmArguments.addAll(jvmArguments);
        ProjectConnection connection = null;
        try {
            // see https://github.com/gradle/gradle-script-kotlin/issues/266
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir).useInstallation(gradleDist);
            connector.useGradleUserHomeDir(gradleUserHome);
            connection = connector.connect();
            return connection.model(KotlinBuildScriptModel.class)
                .setJvmArguments(effectiveJvmArguments)
                .withArguments(arguments)
                .setJavaHome(javaHome)
                .get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public Map<String, Object> getEnvironment(IFile file) {
        HashMap<String, Object> environment = new HashMap<String, Object>();
        FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(file.getProject()).toRequestAttributes();
        File gradleDistributionDir;
        try {
            // see https://github.com/gradle/gradle-script-kotlin/issues/266
            GradleDistribution distribution = attributes.getGradleDistribution();
            Field localInstallationDirField = GradleDistribution.class.getDeclaredField("localInstallationDir");
            localInstallationDirField.setAccessible(true);
            gradleDistributionDir = (File) localInstallationDirField.get(distribution);
            if (gradleDistributionDir == null) {
                throw new RuntimeException("Only local installation is supported");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        environment.put(GSK_PROJECT_ROOT, attributes.getProjectDir());
        environment.put(GSK_GRADLE_HOME, gradleDistributionDir);
        environment.put(NON_GSK_GRADLE_USER_HOME, attributes.getGradleUserHome());
        environment.put(GSK_JAVA_HOME, attributes.getJavaHome());
        environment.put(NON_GSK_OPTIONS, attributes.getArguments());
        environment.put(GSK_JVM_OPTIONS, attributes.getJvmArguments());

        return environment;
    }

    @Override
    public String getTemplateClassName() {
        return "org.gradle.script.lang.kotlin.KotlinBuildScript";
    }
}