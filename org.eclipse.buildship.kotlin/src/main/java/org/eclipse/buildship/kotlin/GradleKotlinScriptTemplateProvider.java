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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.kotlin.dsl.tooling.models.KotlinBuildScriptTemplateModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jetbrains.kotlin.core.model.ScriptTemplateProviderEx;

import com.google.common.collect.Lists;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Contributes the Gradle Kotlin Script template to the Kotlin Eclipse
 * integration.
 *
 * @author Donat Csikos
 */
public final class GradleKotlinScriptTemplateProvider implements ScriptTemplateProviderEx {

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
    public boolean isApplicable(IFile file) {
        IProject project = file.getProject();
        return GradleProjectNature.isPresentOn(project);
    }

    @Override
    public Iterable<String> getTemplateClasspath(Map<String, ? extends Object> environment, IProgressMonitor monitor) {
        KotlinBuildScriptTemplateModel model = queryModel(KotlinBuildScriptTemplateModel.class, environment);
        if (model == null) {
            return Collections.emptyList();
        } else {
            List<File> classpathFiles = model.getClassPath();
            List<String> classpath = Lists.newArrayListWithCapacity(classpathFiles.size());
            for (File classpathFile : classpathFiles) {
                classpath.add(classpathFile.getAbsolutePath());
            }
            return classpath;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T queryModel(Class<T> model, Map<String, ? extends Object> environment) {
        ProjectConnection connection = null;
        try {
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory((File) environment.get(GSK_PROJECT_ROOT));
            connector.useGradleUserHomeDir((File) environment.get(GSK_GRADLE_USER_HOME));
            applyGradleDistribution(environment, connector);
            connection = connector.connect();
            return connection.model(model)
                    .setJvmArguments((List<String>) environment.get(GSK_JVM_OPTIONS))
                    .withArguments((List<String>) environment.get(GSK_OPTIONS))
                    .setJavaHome((File) environment.get(GSK_JAVA_HOME))
                    .get();
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot query BuildEnvironment", e);
            return null;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void applyGradleDistribution(Map<String, ? extends Object> environment, GradleConnector connector) {
        File gradleLocal = (File) environment.get(GSK_INSTALLATION_LOCAL);
        URI gradleRemote = (URI) environment.get(GSK_INSTALLATION_REMOTE);
        String gradleVersion = (String) environment.get(GSK_INSTALLATION_VERSION);
        if (gradleLocal != null) {
            connector.useInstallation(gradleLocal);
        } else if (gradleRemote != null) {
            connector.useDistribution(gradleRemote);
        } else if (gradleVersion != null) {
            connector.useGradleVersion(gradleVersion);
        } else {
            connector.useBuildDistribution();
        }
    }

    @Override
    public Map<String, Object> getEnvironment(IFile file) {
        HashMap<String, Object> environment = new HashMap<>();
        BuildConfiguration buildConfig = CorePlugin.configurationManager().loadProjectConfiguration(file.getProject()).getBuildConfiguration();

        environment.put(GSK_PROJECT_ROOT, buildConfig.getRootProjectDirectory());
        environment.put(GSK_GRADLE_USER_HOME, buildConfig.getWorkspaceConfiguration().getGradleUserHome());
        environment.put(GSK_JAVA_HOME, null);
        environment.put(GSK_OPTIONS, Collections.<String>emptyList());
        environment.put(GSK_JVM_OPTIONS, Collections.<String>emptyList());

        GradleDistribution gradleDistribution = buildConfig.getGradleDistribution();
        switch (gradleDistribution.getDistributionInfo().getType()) {
        case LOCAL_INSTALLATION:
            environment.put(GSK_INSTALLATION_LOCAL, new File(gradleDistribution.getDistributionInfo().getConfiguration()));
            break;
        case REMOTE_DISTRIBUTION:
            environment.put(GSK_INSTALLATION_REMOTE, createURI(gradleDistribution.getDistributionInfo().getConfiguration()));
            break;
        case VERSION:
            environment.put(GSK_INSTALLATION_VERSION, gradleDistribution.getDistributionInfo().getConfiguration());
            break;
        default:
            break;
        }

        return environment;
    }

    @Override
    public String getTemplateClassName() {
        return "org.gradle.kotlin.dsl.KotlinBuildScript";
    }

    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}