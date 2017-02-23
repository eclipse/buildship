/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.ProjectConfiguration.ConversionStrategy;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;

/**
 * Contributes the Gradle Kotlin Script template to the Kotlin Eclipse integration.
 *
 * @author Donat Csikos
 */
public class GradleKotlinScriptTemplateProvider implements ScriptTemplateProviderEx {

    // properties names defined in gradle-script-kotlin
    private static final String GSK_PROJECT_ROOT = "projectRoot";
    private static final String GSK_GRADLE_USER_HOME = "gradleUserHome";
    private static final String GSK_JAVA_HOME = "gradleJavaHome";
    private static final String GSK_OPTIONS = "gradleOptions";
    private static final String GSK_JVM_OPTIONS = "gradleJvmOptions";
    private static final String GSK_INSTALLATION_LOCAL = "gradleHome";
    private static final String GSK_INSTALLATION_REMOTE = "gradleUri";
    private static final String GSK_INSTALLATION_VERSION = "gradleVersion";

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> getTemplateClasspath(Map<String, ? extends Object> environment) {
        File projectDir = (File) environment.get(GSK_PROJECT_ROOT);
        File gradleUserHome = (File) environment.get(GSK_GRADLE_USER_HOME);
        File javaHome = (File)environment.get(GSK_JAVA_HOME);
        List<String> arguments = (List<String>) environment.get(GSK_OPTIONS);
        List<String> jvmArguments = (List<String>) environment.get(GSK_JVM_OPTIONS);

        GradleDistribution gradleDistribution = gradleDistributionFor(environment);

        List<String> effectiveJvmArguments = Lists.newArrayList("-Dorg.gradle.script.lang.kotlin.provider.mode=classpath"); // from KotlinScriptPluginFactory
        effectiveJvmArguments.addAll(jvmArguments);

        KotlinBuildScriptModel model = queryTemplateClasspath(projectDir, gradleDistribution, gradleUserHome, javaHome, arguments, effectiveJvmArguments);

        List<String> result = Lists.newArrayList();
        if (model != null) {
            for (File f : model.getClassPath()) {
                result.add(f.getAbsolutePath());
            }
        }
        return result;
    }

    private static GradleDistribution gradleDistributionFor(Map<String, ? extends Object> environment) {
        File gradleLocal = (File) environment.get(GSK_INSTALLATION_LOCAL);
        URI gradleRemote = (URI) environment.get(GSK_INSTALLATION_REMOTE);
        String gradleVersion = (String) environment.get(GSK_INSTALLATION_VERSION);
        if (gradleLocal != null) {
            return GradleDistribution.forLocalInstallation(gradleLocal);
        } else if (gradleRemote != null) {
            return GradleDistribution.forRemoteDistribution(gradleRemote);
        } else if (gradleVersion != null) {
            return GradleDistribution.forVersion(gradleVersion);
        } else {
            return GradleDistribution.fromBuild();
        }
    }

    private static KotlinBuildScriptModel queryTemplateClasspath(File projectDir, GradleDistribution gradleDistribution, File gradleUserHome, File javaHome, List<String> arguments, List<String> jvmArguments) {
        ProjectConnection connection = null;
        try {
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir);
            connector.useGradleUserHomeDir(gradleUserHome);
            gradleDistribution.apply(connector);
            connection = connector.connect();
            return connection.model(KotlinBuildScriptModel.class)
                .setJvmArguments(jvmArguments)
                .withArguments(arguments)
                .setJavaHome(javaHome)
                .get();
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot query Kotlin build script classpath model", e);
            return null;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public Map<String, Object> getEnvironment(IFile file) {
        HashMap<String, Object> environment = new HashMap<String, Object>();
        FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(file.getProject()).toRequestAttributes(ConversionStrategy.MERGE_WORKSPACE_SETTINGS);

        environment.put(GSK_PROJECT_ROOT, attributes.getProjectDir());
        environment.put(GSK_GRADLE_USER_HOME, attributes.getGradleUserHome());
        environment.put(GSK_JAVA_HOME, attributes.getJavaHome());
        environment.put(GSK_OPTIONS, attributes.getArguments());
        environment.put(GSK_JVM_OPTIONS, attributes.getJvmArguments());

        GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper.from(attributes.getGradleDistribution());
        switch (gradleDistribution.getType()) {
            case LOCAL_INSTALLATION:
                environment.put(GSK_INSTALLATION_LOCAL, new File(gradleDistribution.getConfiguration()));
                break;
            case REMOTE_DISTRIBUTION:
                environment.put(GSK_INSTALLATION_REMOTE, createURI(gradleDistribution.getConfiguration()));
                break;
            case VERSION:
                environment.put(GSK_INSTALLATION_VERSION, gradleDistribution.getConfiguration());
                break;
            default:
                break;
        }

        return environment;
    }

    @Override
    public String getTemplateClassName() {
        return "org.gradle.script.lang.kotlin.KotlinBuildScript";
    }

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}