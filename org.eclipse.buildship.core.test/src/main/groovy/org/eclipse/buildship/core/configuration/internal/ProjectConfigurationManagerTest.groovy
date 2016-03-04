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

package org.eclipse.buildship.core.configuration.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.junit.TestFile
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.buildship.core.workspace.WorkspaceOperations

@SuppressWarnings("GroovyAccessibility")
class ProjectConfigurationManagerTest extends ProjectSynchronizationSpecification {

    @Shared
    ProjectConfigurationManager configurationManager = CorePlugin.projectConfigurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

    def "no Gradle root project configurations available when there are no projects"() {
        setup:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no Eclipse projects with Gradle nature"() {
        given:
        newProject("sample-project")

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no open Eclipse projects with Gradle nature"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        project.close(null)

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "one Gradle root project configuration when one Gradle multi-project build is imported"() {
        setup:
        def rootDir = dir("root") {
            file('settings.gradle').text = '''
                rootProject.name = 'project one'
                include 'sub1'
                include 'sub2'
            '''
            sub1 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            sub2 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        importAndWait(rootDir)

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDir,
                                null,
                                GradleDistribution.fromBuild(),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':'))] as Set
    }

    def "two Gradle root project configurations when two Gradle multi-project builds are imported"() {
        setup:
        def rootDirOne = dir("root1") {
            file('settings.gradle').text = '''
                rootProject.name = 'project one'
                include 'sub1'
                include 'sub2'
            '''
            sub1 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            sub2 {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        def rootDirTwo = dir("root2") {
            file('settings.gradle').text = '''
                rootProject.name = 'project two'
                include 'alpha'
                include 'beta'
            '''
            alpha {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
            beta {
                file('build.gradle').text = '''
                   apply plugin: 'java'
                '''
            }
        }

        importAndWait(rootDirOne)
        importAndWait(rootDirTwo, GradleDistribution.forVersion("1.12"))

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations()

        then:
        rootProjectConfigurations == [
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDirOne,
                                null,
                                GradleDistribution.fromBuild(),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':')),
                ProjectConfiguration.from(
                        new FixedRequestAttributes(rootDirTwo,
                                null,
                                GradleDistribution.forVersion('1.12'),
                                null,
                                ImmutableList.of(),
                                ImmutableList.of()),
                        Path.from(':'))] as Set
    }

    def "error thrown when projects of same multi-project build have different shared project configurations"() {
        given:
        // create root project and use Gradle version 2.0 in the persisted configuration
        IProject rootProject = workspaceOperations.createProject("root-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def requestAttributes = new FixedRequestAttributes(testDir, null, GradleDistribution.forVersion("2.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"))
        configurationManager.saveProjectConfiguration(projectConfiguration, rootProject)

        // create child project and use Gradle version 1.0 in the persisted configuration
        IProject childProject = workspaceOperations.createProject("child-project", dir("child-project"), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def childRequestAttributes = new FixedRequestAttributes(testDir, null, GradleDistribution.forVersion("1.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def childProjectConfiguration = ProjectConfiguration.from(childRequestAttributes, Path.from(":child"))
        configurationManager.saveProjectConfiguration(childProjectConfiguration, childProject)

        when:
        configurationManager.getRootProjectConfigurations()

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "save and read project with full configuration"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def requestAttributes = new FixedRequestAttributes(project.getLocation().toFile(), tempFolderProvider.newFolder(), GradleDistribution.forVersion("1.12"), tempFolderProvider.newFolder(),
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"))

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "save and read project with minimal configuration"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def attributes = new FixedRequestAttributes(project.getLocation().toFile(), null, GradleDistribution.fromBuild(), null,
                ImmutableList.of(), ImmutableList.of())
        def projectConfiguration = ProjectConfiguration.from(attributes, Path.from(":"))

        when:
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(project) == projectConfiguration
    }

    def "project configuration can be read even if project is not yet refreshed"() {
        given:
        IProject project = workspaceOperations.createProject("sample-project", testDir, Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def attributes = new FixedRequestAttributes(project.getLocation().toFile(), null, GradleDistribution.fromBuild(), null,
                ImmutableList.of(), ImmutableList.of())
        def projectConfiguration = ProjectConfiguration.from(attributes, Path.from(":"))
        configurationManager.saveProjectConfiguration(projectConfiguration, project)

        def projectDescription = project.description
        project.delete(false, true, null)
        project.create(projectDescription, null)
        project.open(IResource.BACKGROUND_REFRESH, null)

        when:
        def readConfiguration = configurationManager.readProjectConfiguration(project)

        then:
        readConfiguration == projectConfiguration
    }

}
