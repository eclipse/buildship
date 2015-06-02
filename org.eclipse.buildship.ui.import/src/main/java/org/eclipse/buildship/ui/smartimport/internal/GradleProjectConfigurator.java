/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat) - Smart Import feature
 */

package org.eclipse.buildship.ui.smartimport.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.projectimport.ProjectImportJob;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

public class GradleProjectConfigurator implements ProjectConfigurator {

    @Override
    public boolean canConfigure(IProject project, Set<IPath> ignoredPaths,
            IProgressMonitor monitor) {
        return shouldBeAnEclipseProject(project, monitor);
    }

    @Override
    public void configure(IProject project, Set<IPath> excludedDirectories,
            IProgressMonitor monitor) {
        if (!shouldBeAnEclipseProject(project, monitor)) {
            return;
        }

        ProjectImportConfiguration configuration = createGradleProjectConfigurations(project);
        importProject(project, configuration, monitor);
    }

    /**
     * Sets up the Gradle configurations for the given project.
     * @param project The given project.
     */
    private ProjectImportConfiguration createGradleProjectConfigurations(
            IProject project) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration();
        GradleDistributionWrapper gradleDistribution = GradleDistributionWrapper
                .from(DistributionType.WRAPPER, null);

        configuration.setGradleDistribution(gradleDistribution);
        configuration.setApplyWorkingSets(true);
        configuration.setWorkingSets(new ArrayList<String>(0));
        configuration.setProjectDir(project.getLocation().toFile());

        return configuration;
    }

    /**
     * Imports the given project according to the configuration.
     * @param project The given project.
     * @param configuration The project's configurations.
     * @param monitor
     */
    private void importProject(IProject project,
            ProjectImportConfiguration configuration, IProgressMonitor monitor) {
        ProjectImportJob projectImportJob = new ProjectImportJob(configuration, AsyncHandler.NO_OP);
        projectImportJob.schedule();
    }

    @Override
    public boolean shouldBeAnEclipseProject(IContainer container,
            IProgressMonitor monitor) {
        IFile gradleBuildFile = container.getFile(new Path("build.gradle"));
        IFile gradleShellScript = container.getFile(new Path("gradlew"));
        IFile gradleBatchFile = container.getFile(new Path("gradlew.bat"));

        return gradleBuildFile.exists()
                && (gradleShellScript.exists() || gradleBatchFile.exists());
    }

    @Override
    public Set<IFolder> getDirectoriesToIgnore(IProject project,
            IProgressMonitor monitor) {
        Set<IFolder> res = new HashSet<IFolder>(3);

        res.add(project.getFolder("src"));
        res.add(project.getFolder("build"));
        res.add(project.getFolder("target"));
        return res;
    }

    @Override
    public IWizard getConfigurationWizard() {
        return null;
    }

}
