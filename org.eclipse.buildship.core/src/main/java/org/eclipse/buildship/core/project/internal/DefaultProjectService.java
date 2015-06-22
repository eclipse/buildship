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
import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager;
import org.eclipse.buildship.core.gradle.model.GradleModel;
import org.eclipse.buildship.core.gradle.model.internal.GradleModelImpl;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.project.ProjectConversionExtension;
import org.eclipse.buildship.core.project.ProjectService;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Default implementation of the {@link ProjectService}.
 *
 */
public class DefaultProjectService implements ProjectService {

	private static final String GRADLE_CONVERSION_EXTENSION_ID = CorePlugin.PLUGIN_ID + ".gradleconversionextension";
	private static final String GRADLE_CONVERSION_EXTENSION_NATURE_ATTRIBUTE = "nature";

	private WorkspaceOperations workspaceOperations;
	private ProjectConfigurationManager projectConfigurationManager;
	private Logger logger;

	public DefaultProjectService(WorkspaceOperations workspaceOperations,
			ProjectConfigurationManager projectConfigurationManager, Logger logger) {
		this.workspaceOperations = Preconditions.checkNotNull(workspaceOperations);
		this.projectConfigurationManager = Preconditions.checkNotNull(projectConfigurationManager);
		this.logger = Preconditions.checkNotNull(logger);
	}

	@Override
	public void convertToGradleProject(IProgressMonitor progressMonitor,
			GradleRunConfigurationAttributes configurationAttributes, IProject project)
					throws Exception {

		if (project.hasNature(GradleProjectNature.ID)) {
			this.logger.info("Tried to convert " + project.getName()
					+ " project, but it has already the Gradle project nature.");
			progressMonitor.done();
			return;
		}

		SubMonitor mainMonitor = SubMonitor.convert(progressMonitor, "Converting project to a Gradle project", 100);

		// create Gradle files for the project
		File gradleFilesDirectory = this.createGradleFileForProject(configurationAttributes, project, mainMonitor);
		mainMonitor.setWorkRemaining(70);

		// copy gradle Gradle files from gradleFilesDirectory directory to the
		// project
		this.workspaceOperations.copyFileToContainer(mainMonitor.newChild(20), gradleFilesDirectory, project, true);
		mainMonitor.setWorkRemaining(50);

		// persist the Gradle-specific configuration in the Eclipse project's
		// .settings folder
		FixedRequestAttributes fixedRequestAttributes = new FixedRequestAttributes(project.getLocation().toFile(),
				configurationAttributes.getGradleUserHome(), configurationAttributes.getGradleDistribution(),
				configurationAttributes.getJavaHome(), configurationAttributes.getJvmArguments(),
				configurationAttributes.getArguments());
		ProjectConfiguration projectConfiguration = ProjectConfiguration.from(fixedRequestAttributes, Path.from(":"),
				project.getLocation().toFile());
		this.projectConfigurationManager.saveProjectConfiguration(projectConfiguration, project);
		mainMonitor.setWorkRemaining(20);

		// add the gradle nature to the project
		this.workspaceOperations.addNature(project, GradleProjectNature.ID, mainMonitor.newChild(20));

		mainMonitor.done();
	}

	private File createGradleFileForProject(GradleRunConfigurationAttributes configurationAttributes, IProject project,
			SubMonitor mainMonitor) throws Exception {
		List<ProjectConversionExtension> projectConversionExtensions = this.getProjectConversionExtensions(project);
		GradleModel gradleModel = new GradleModelImpl();
		gradleModel.setRootProjectName(project.getName());
		for (ProjectConversionExtension projectConversionExtension : projectConversionExtensions) {
			projectConversionExtension.addProjectSpecificInformation(mainMonitor, project, gradleModel);
		}

		return GradleInitializer.getInitializedGradleFiles(mainMonitor.newChild(30),
					configurationAttributes, gradleModel);
	}

	private List<ProjectConversionExtension> getProjectConversionExtensions(IProject project) throws CoreException {
		Builder<ProjectConversionExtension> projectConversionExtensions = ImmutableList
				.<ProjectConversionExtension> builder();
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] configurationElements = extensionRegistry
				.getConfigurationElementsFor(GRADLE_CONVERSION_EXTENSION_ID);
		for (IConfigurationElement configurationElement : configurationElements) {
			String nature = configurationElement.getAttribute(GRADLE_CONVERSION_EXTENSION_NATURE_ATTRIBUTE);
			if (nature == null || project.hasNature(nature)) {
				ProjectConversionExtension executableExtension = (ProjectConversionExtension) configurationElement.createExecutableExtension("class");
				projectConversionExtensions.add(executableExtension);
			}
		}
		return projectConversionExtensions.build();
	}

}
