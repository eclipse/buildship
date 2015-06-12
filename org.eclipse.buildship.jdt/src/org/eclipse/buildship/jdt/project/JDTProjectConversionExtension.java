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

package org.eclipse.buildship.jdt.project;

import java.util.List;

import org.eclipse.buildship.core.gradle.model.Dependency;
import org.eclipse.buildship.core.gradle.model.GradleModel;
import org.eclipse.buildship.core.gradle.model.GradleModelFactory;
import org.eclipse.buildship.core.gradle.model.Plugin;
import org.eclipse.buildship.core.gradle.model.SourceSet;
import org.eclipse.buildship.core.project.ProjectConversionExtension;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

/**
 * This {@link ProjectConversionExtension} adds the java specific options to the
 * build.
 */
public class JDTProjectConversionExtension implements ProjectConversionExtension {

	@Override
	public void addProjectSpecificInformation(IProgressMonitor monitor, IProject project, GradleModel model) throws Exception {
		if(!project.hasNature(JavaCore.NATURE_ID)) {
			// do nothing if the nature is not suitable
			return;
		}
		SubMonitor mainMonitor = SubMonitor.convert(monitor, "Add JDT specific conversion information", 99);

		Plugin gradleJavaPlugin = GradleModelFactory.createPlugin("java");
		model.addPlugins(ImmutableList.<Plugin> of(gradleJavaPlugin));

		mainMonitor.setWorkRemaining(66);
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		List<Dependency> dependencies = this.getRawDependencies(rawClasspath, project.getFullPath());
		model.addDependencies(dependencies);

		mainMonitor.setWorkRemaining(33);
		List<SourceSet> sourceSets = this.getSourceSets(javaProject);
		model.addSourceSets(sourceSets);
	}

	private List<SourceSet> getSourceSets(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();

		return FluentIterable.<IPackageFragmentRoot> of(packageFragmentRoots)
				.filter(new com.google.common.base.Predicate<IPackageFragmentRoot>() {

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
						return GradleModelFactory.createSourceSet("java", packageFragmentRoot.getElementName());
					}
				}).toList();
	}

	private List<Dependency> getRawDependencies(IClasspathEntry[] rawClasspath, final IPath projectPath) {
		if (rawClasspath.length < 1) {
			return ImmutableList.<Dependency> of();
		}
		List<Dependency> dependencies = FluentIterable.<IClasspathEntry> of(rawClasspath)
				.transform(new Function<IClasspathEntry, Dependency>() {

					@Override
					public Dependency apply(IClasspathEntry classpathEntry) {
						return GradleModelFactory.createDependeny(classpathEntry.getPath().makeRelativeTo(projectPath).toPortableString());
					}
				}).filter(new com.google.common.base.Predicate<Dependency>() {

					@Override
					public boolean apply(Dependency dependency) {
						// We currently only support file referenced
						// dependencies
						return dependency.getDependencyString().endsWith(".jar");
					}
				}).toList();
		return dependencies;
	}
}
