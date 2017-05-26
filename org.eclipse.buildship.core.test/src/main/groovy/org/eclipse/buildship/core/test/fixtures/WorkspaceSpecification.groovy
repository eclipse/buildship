/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.test.fixtures

import java.io.File

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.AutoCleanup
import spock.lang.Specification

import com.google.common.io.Files

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.ConfigurationManager

/**
 * Base Spock test specification to verify Buildship functionality against the current state of the
 * workspace.
 */
abstract class WorkspaceSpecification extends Specification {

    @Rule
    TemporaryFolder tempFolderProvider

    @AutoCleanup
    TestEnvironment environment = TestEnvironment.INSTANCE

    private File externalTestDir

    void setup() {
        externalTestDir = tempFolderProvider.newFolder('external')
    }

    void cleanup() {
        deleteAllProjects(true)
    }

    protected void deleteAllProjects(boolean includingContent) {
        for (IProject project : CorePlugin.workspaceOperations().allProjects) {
            project.delete(includingContent, true, null);
        }
        workspaceDir.listFiles().findAll { it.isDirectory() && it.name != '.metadata' }.each { File it -> it.deleteDir() }
    }

    protected <T> void registerService(Class<T> serviceType, T implementation) {
        environment.registerService(serviceType, implementation)
    }

    protected FileTreeBuilder fileTree(File baseDir) {
        new FileTreeBuilder(baseDir)
    }

    protected File fileTree(File baseDir, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        fileTree(baseDir).call(config)
    }

    protected File dir(String location) {
        def dir = getDir(location)
        dir.mkdirs()
        return dir
    }

    protected File dir(String location, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return fileTree(dir(location), config)
    }

    protected File getDir(String location) {
        return new File(testDir, location)
    }

    protected File getTestDir() {
        externalTestDir
    }

    protected File file(String location) {
        def file = getFile(location)
        Files.touch(file)
        return file
    }

    protected File getFile(String location) {
        return new File(testDir, location)
    }


    protected File workspaceDir(String location) {
        def dir = getWorkspaceDir(location)
        dir.mkdirs()
        return dir
    }

    protected File workspaceDir(String location, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return fileTree(workspaceDir(location), config)
    }

    protected File getWorkspaceDir(String location) {
        return new File(workspaceDir, location)
    }

    protected IWorkspace getWorkspace() {
        LegacyEclipseSpockTestHelper.workspace
    }

    protected File getWorkspaceDir() {
        workspace.root.location.toFile()
    }

    protected File workspaceFile(String location) {
        def file = getWorkspaceFile(location)
        Files.touch(file)
        return file
    }

    protected File getWorkspaceFile(String location) {
        return new File(workspaceDir, location)
    }

    protected IProject newClosedProject(String name) {
        EclipseProjects.newClosedProject(name, dir(name))
    }

    protected IProject newProject(String name) {
        EclipseProjects.newProject(name, dir(name))
    }

    protected IJavaProject newJavaProject(String name) {
        EclipseProjects.newJavaProject(name, dir(name))
    }

    protected List<IProject> allProjects() {
        workspace.root.projects
    }

    protected IProject findProject(String name) {
        CorePlugin.workspaceOperations().findProjectByName(name).orNull()
    }

    protected IJavaProject findJavaProject(String name) {
        IProject project = findProject(name)
        return project == null ? null : JavaCore.create(project)
    }

    protected ConfigurationManager getConfigurationManager() {
        CorePlugin.configurationManager()
    }

    protected BuildConfiguration createInheritingBuildConfiguration(File projectDir) {
        configurationManager.createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), null, false, false, false)
    }

    protected BuildConfiguration createOverridingBuildConfiguration(File projectDir, GradleDistribution distribution = GradleDistribution.fromBuild(),
                                                                  boolean buildScansEnabled = false, boolean offlineMode = false, File gradleUserHome = null) {
        configurationManager.createBuildConfiguration(projectDir, distribution, gradleUserHome, true, buildScansEnabled, offlineMode)
    }
}
