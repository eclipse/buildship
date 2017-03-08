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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration.ConversionStrategy;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.jetbrains.kotlin.core.model.ScriptTemplateProviderEx;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Contributes the Gradle Kotlin Script template to the Kotlin Eclipse
 * integration.
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
	public boolean isApplicable(IFile file) {
		IProject project = file.getProject();
		return GradleProjectNature.isPresentOn(project);
	}

	@Override
	public Iterable<String> getTemplateClasspath(Map<String, ? extends Object> environment, IProgressMonitor monitor) {
		BuildEnvironment buildEnvironment = queryBuildEnvironment(environment);
		GradleEnvironment gradleEnvironment = buildEnvironment.getGradle();

		File gradleUserHome = gradleEnvironment.getGradleUserHome();
		String gradleVersion = gradleEnvironment.getGradleVersion();
		File distroRoot = findDistributionRoot(gradleUserHome, gradleVersion);

		if (distroRoot == null) {
			return Collections.emptyList();
		}

		List<String> result = Lists.newArrayList();
		addGradleScriptKotlinJar(distroRoot, result);
		addGeneratedGradleJars(gradleUserHome, gradleVersion, result);

		// TODO (donat) add the ${gradleUserHome}/caches/3.5-20170305000422+0000/gradle-script-kotlin/${scriptMd5Hash} to the classpath
		// The classes defining the accessors (`application { ... }` and friends) are compiled dynamically to that folder
		// The related MD5 calculation logic in GSK: https://github.com/gradle/gradle-script-kotlin/blob/98ec7c0c42f740ef14c21aa39a2bc9ae98f96397/src/main/kotlin/org/gradle/script/lang/kotlin/resolver/KotlinBuildScriptDependenciesResolver.kt#L72-L92
		return result;
	}

	@SuppressWarnings("unchecked")
	private static BuildEnvironment queryBuildEnvironment(Map<String, ? extends Object> environment) {
		List<String> jvmArguments = Lists.newArrayList((List<String>) environment.get(GSK_JVM_OPTIONS));
		jvmArguments.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=debug");
		List<String> effectiveJvmArguments = Lists.newArrayList("-Dorg.gradle.script.lang.kotlin.provider.mode=classpath"); // from  KotlinScriptPluginFactory
		effectiveJvmArguments.addAll(jvmArguments);

		ProjectConnection connection = null;
		try {
			GradleConnector connector = GradleConnector.newConnector().forProjectDirectory((File) environment.get(GSK_PROJECT_ROOT));
			connector.useGradleUserHomeDir((File) environment.get(GSK_GRADLE_USER_HOME));
			applyGradleDistribution(environment, connector);
			connection = connector.connect();
			return connection.model(BuildEnvironment.class)
					.setJvmArguments(effectiveJvmArguments)
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

	private static void addGradleScriptKotlinJar(File distroRoot, List<String> result) {
		for (File f1 : Files.fileTreeTraverser().breadthFirstTraversal(distroRoot)) {
			if (f1.isDirectory() && f1.getName().equals("lib")) {
				for (File f2 : f1.listFiles()) {
					if (f2.getName().endsWith(".jar") && f2.getName().startsWith("gradle-script-kotlin")) {
						result.add(f2.getAbsolutePath());
						return;
					}
				}
			}
		}
		CorePlugin.logger().warn("Can't find gradle-script-kotlin jar in " + distroRoot.getAbsolutePath());
	}

	private static void addGeneratedGradleJars(File gradleUserHome, String gradleVersion, List<String> result) {
		File cacheDir = new File(gradleUserHome, "caches/" + gradleVersion);
		File generatedJarsDir = new File(cacheDir, "generated-gradle-jars");
		if (generatedJarsDir.exists()) {
			for (File f : Files.fileTreeTraverser().breadthFirstTraversal(generatedJarsDir)) {
				if (f.getName().endsWith("jar")) {
					result.add(f.getAbsolutePath());
				}
			}
		}
	}

	@Override
	public Map<String, Object> getEnvironment(IFile file) {
		HashMap<String, Object> environment = new HashMap<String, Object>();
		FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager()
				.readProjectConfiguration(file.getProject())
				.toRequestAttributes(ConversionStrategy.MERGE_WORKSPACE_SETTINGS);

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