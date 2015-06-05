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
import java.io.IOException;
import java.util.List;

import com.gradleware.tooling.toolingmodel.Path;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.project.ProjectService;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;

/**
 * Default implementation of the {@link ProjectService}.
 *
 */
public class DefaultProjectService implements ProjectService {

    private WorkspaceOperations workspaceOperations;
    private ProjectConfigurationManager projectConfigurationManager;
    private Logger logger;

    public DefaultProjectService(WorkspaceOperations workspaceOperations, ProjectConfigurationManager projectConfigurationManager, Logger logger) {
        this.workspaceOperations = Preconditions.checkNotNull(workspaceOperations);
        this.projectConfigurationManager = Preconditions.checkNotNull(projectConfigurationManager);
        this.logger = Preconditions.checkNotNull(logger);
    }

    @Override
    public void convertToGradleProject(IProgressMonitor progressMonitor, GradleRunConfigurationAttributes configurationAttributes, IProject project) throws CoreException,
            IOException {

        if (project.hasNature(GradleProjectNature.ID)) {
            this.logger.info("Tried to convert " + project.getName() + " project, but it has already the Gradle project nature.");
            progressMonitor.done();
            return;
        }

        SubMonitor mainMonitor = SubMonitor.convert(progressMonitor, "Converting project to a Gradle project", 100);

        // create Gradle files for the project
        File gradleFilesDirectory = createGradleFileForProject(configurationAttributes, project, mainMonitor);
        mainMonitor.setWorkRemaining(70);

        // copy gradle Gradle files from gradleFilesDirectory directory to the project
        this.workspaceOperations.copyFileToContainer(mainMonitor.newChild(20), gradleFilesDirectory, project, true);
        mainMonitor.setWorkRemaining(50);

        // persist the Gradle-specific configuration in the Eclipse project's .settings folder
        FixedRequestAttributes fixedRequestAttributes = new FixedRequestAttributes(project.getLocation().toFile(), configurationAttributes.getGradleUserHome(),
                configurationAttributes.getGradleDistribution(), configurationAttributes.getJavaHome(), configurationAttributes.getJvmArguments(),
                configurationAttributes.getArguments());
        ProjectConfiguration projectConfiguration = ProjectConfiguration.from(fixedRequestAttributes, Path.from(":"), project.getLocation().toFile());
        this.projectConfigurationManager.saveProjectConfiguration(projectConfiguration, project);
        mainMonitor.setWorkRemaining(20);

        // add the gradle nature to the project
        this.workspaceOperations.addNature(project, GradleProjectNature.ID, mainMonitor.newChild(20));

        mainMonitor.done();
    }

    private File createGradleFileForProject(GradleRunConfigurationAttributes configurationAttributes, IProject project, SubMonitor mainMonitor) throws CoreException,
            JavaModelException, IOException {
        File gradleFilesDirectory = null;
        if (project.hasNature(JavaCore.NATURE_ID)) {
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
            List<String> dependencies = getRawDependencies(rawClasspath, project.getFullPath());
            List<SourceSet> sourceSets = getSourceSets(javaProject);
            gradleFilesDirectory = GradleInitializer.getInitializedGradleFiles(mainMonitor.newChild(30), configurationAttributes, project.getName(), sourceSets,
                    ImmutableList.<String> of("java"), ImmutableList.<String> of("jcenter()"), dependencies);
        } else {
            gradleFilesDirectory = GradleInitializer.getInitializedGradleFiles(mainMonitor.newChild(30), configurationAttributes, project.getName(),
                    ImmutableList.<SourceSet> of(), ImmutableList.<String> of(), ImmutableList.<String> of(), ImmutableList.<String> of());
        }
        return gradleFilesDirectory;
    }

    private List<SourceSet> getSourceSets(IJavaProject javaProject) throws JavaModelException {
        IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();

        return FluentIterable.<IPackageFragmentRoot> of(packageFragmentRoots).filter(new Predicate<IPackageFragmentRoot>() {

            @Override
            public boolean apply(IPackageFragmentRoot arg0) {
                IResource fragmentRoot;
                try {
                    fragmentRoot = arg0.getCorrespondingResource();
                    return fragmentRoot != null && (fragmentRoot.getType() != IResource.FILE);
                } catch (JavaModelException e) {
                    // ignore
                }
                return false;
            }
        }).transform(new Function<IPackageFragmentRoot, SourceSet>() {

            @Override
            public SourceSet apply(IPackageFragmentRoot packageFragmentRoot) {
                SourceSet sourceSet = new SourceSet();
                sourceSet.setName("java");
                sourceSet.setPath(packageFragmentRoot.getElementName());
                return sourceSet;
            }
        }).toList();
    }

    private List<String> getRawDependencies(IClasspathEntry[] rawClasspath, final IPath projectPath) {
        if (rawClasspath.length < 1) {
            return ImmutableList.<String> of();
        }
        List<String> dependencies = FluentIterable.<IClasspathEntry> of(rawClasspath).transform(new Function<IClasspathEntry, String>() {

            @Override
            public String apply(IClasspathEntry classpathEntry) {
                return classpathEntry.getPath().makeRelativeTo(projectPath).toPortableString();
            }
        }).filter(new Predicate<String>() {

            @Override
            public boolean apply(String classPath) {
                // We currently only support file referenced dependencies
                return classPath.endsWith(".jar");
            }
        }).toList();
        return dependencies;
    }

}
