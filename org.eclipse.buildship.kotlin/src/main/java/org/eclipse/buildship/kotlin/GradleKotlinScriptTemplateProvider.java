/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.jetbrains.kotlin.core.model.ScriptTemplateProviderEx;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration.ConversionStrategy;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;

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
        // TODO (donat) the Gradle home should be available in the BuildEnvirontment TAPI model
        File distroRoot = (File) environment.get(GSK_INSTALLATION_LOCAL);
        if (distroRoot == null) {
            BuildEnvironment buildEnvironment = queryBuildEnvironment(environment);
            GradleEnvironment gradleEnvironment = buildEnvironment.getGradle();
            File gradleUserHome = gradleEnvironment.getGradleUserHome();
            String gradleVersion = gradleEnvironment.getGradleVersion();
            distroRoot = findDistributionRoot(gradleUserHome, gradleVersion);
        }
        if (distroRoot == null) {
            return Collections.emptyList();
        }
        return jarPathsFromDistributionLibDirectory(distroRoot);
    }

    @SuppressWarnings("unchecked")
    private static BuildEnvironment queryBuildEnvironment(Map<String, ? extends Object> environment) {
        ProjectConnection connection = null;
        try {
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory((File) environment.get(GSK_PROJECT_ROOT));
            connector.useGradleUserHomeDir((File) environment.get(GSK_GRADLE_USER_HOME));
            applyGradleDistribution(environment, connector);
            connection = connector.connect();
            return connection.model(BuildEnvironment.class)
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

    private static File findDistributionRoot(File gradleUserHome, final String version) {
        File distsDir = new File(gradleUserHome, "wrapper/dists");
        List<File> candidates = Arrays.asList(distsDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File path) {
                String name = path.getName();
                return name.contains("gradle") && name.contains(version);
            }
        }));

        if (candidates.isEmpty()) {
            return null;
        } else {
            return candidates.get(candidates.size() - 1);
        }
    }

    private static List<String> jarPathsFromDistributionLibDirectory(File distroRoot) {
        Optional<File> libFolder = findLibFolder(distroRoot);
        List<String> result = Lists.newArrayList();

        if (libFolder.isPresent()) {
            for (File jar : libFolder.get().listFiles()) {
                String name = jar.getName();
                if (name.endsWith(".jar")) {
                    result.add(jar.getAbsolutePath());
                }
            }
        }
        return result;
    }

    private static Optional<File> findLibFolder(File distroRoot) {
        return Files.fileTreeTraverser().breadthFirstTraversal(distroRoot).firstMatch(new Predicate<File>() {

            @Override
            public boolean apply(File f1) {
                return f1.isDirectory() && f1.getName().equals("lib");
            }
        });
    }

    @Override
    public Map<String, Object> getEnvironment(IFile file) {
        HashMap<String, Object> environment = new HashMap<String, Object>();
        FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(file.getProject()).toRequestAttributes(ConversionStrategy.MERGE_PROJECT_SETTINGS);

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