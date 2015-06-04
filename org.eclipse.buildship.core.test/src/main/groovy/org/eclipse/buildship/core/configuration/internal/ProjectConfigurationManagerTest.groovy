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

import com.gradleware.tooling.junit.TestFile
import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import com.google.common.collect.ImmutableList

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.configuration.ProjectConfigurationManager
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.workspace.WorkspaceOperations

class ProjectConfigurationManagerTest extends Specification {

    @Shared
    ProjectConfigurationManager configurationManager = CorePlugin.projectConfigurationManager()

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
    }

    def "no Gradle root project configurations available when there are no projects"() {
        setup:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations(new NullProgressMonitor())
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no Eclipse projects with Gradle nature"() {
        given:
        File projectDir = tempFolder.root
        workspaceOperations.createProject("sample-project", projectDir, ImmutableList.of(), ImmutableList.of(), new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations(new NullProgressMonitor())

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "no Gradle root project configurations available when there are no open Eclipse projects with Gradle nature"() {
        given:
        File projectDir = tempFolder.root
        IProject project = workspaceOperations.createProject("sample-project", projectDir, ImmutableList.of(), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        project.close(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations(new NullProgressMonitor())

        then:
        assert rootProjectConfigurations == [] as Set
    }

    def "one Gradle root project configuration when one Gradle multi-project build is imported"() {
        setup:
        def rootDir = new TestFile(tempFolder.newFolder())
        rootDir.create {
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

        def importConfigurationOne = new ProjectImportConfiguration()
        importConfigurationOne.projectDir = new File(rootDir.absolutePath)
        importConfigurationOne.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.WRAPPER, null)
        importConfigurationOne.workingSets = []

        new ProjectImportJob(importConfigurationOne).runToolingApiJobInWorkspace(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations(new NullProgressMonitor())

        then:
        rootProjectConfigurations == [
            ProjectConfiguration.from(
            new FixedRequestAttributes(rootDir,
            null,
            GradleDistribution.fromBuild(),
            null,
            ImmutableList.of(),
            ImmutableList.of()),
            Path.from(':'), rootDir)] as Set
    }

    def "two Gradle root project configurations when two Gradle multi-project builds are imported"() {
        setup:
        def rootDirOne = new TestFile(tempFolder.newFolder())
        rootDirOne.create {
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

        def rootDirTwo = new TestFile(tempFolder.newFolder())
        rootDirTwo.create {
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

        def importConfigurationOne = new ProjectImportConfiguration()
        importConfigurationOne.projectDir = new File(rootDirOne.absolutePath)
        importConfigurationOne.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.WRAPPER, null)
        importConfigurationOne.workingSets = []

        def importConfigurationTwo = new ProjectImportConfiguration()
        importConfigurationTwo.projectDir = new File(rootDirTwo.absolutePath)
        importConfigurationTwo.gradleDistribution = GradleDistributionWrapper.from(GradleDistributionWrapper.DistributionType.VERSION, '1.12')
        importConfigurationTwo.workingSets = []

        new ProjectImportJob(importConfigurationOne).runToolingApiJobInWorkspace(new NullProgressMonitor())
        new ProjectImportJob(importConfigurationTwo).runToolingApiJobInWorkspace(new NullProgressMonitor())

        when:
        Set<ProjectConfiguration> rootProjectConfigurations = configurationManager.getRootProjectConfigurations(new NullProgressMonitor())

        then:
        rootProjectConfigurations == [
            ProjectConfiguration.from(
            new FixedRequestAttributes(rootDirOne,
            null,
            GradleDistribution.fromBuild(),
            null,
            ImmutableList.of(),
            ImmutableList.of()),
            Path.from(':'), rootDirOne),
            ProjectConfiguration.from(
            new FixedRequestAttributes(rootDirTwo,
            null,
            GradleDistribution.forVersion('1.12'),
            null,
            ImmutableList.of(),
            ImmutableList.of()),
            Path.from(':'), rootDirTwo)] as Set
    }

    def "error thrown when projects of same multi-project build have different shared project configurations"() {
        given:
        File rootProjectDir = tempFolder.newFolder()

        // create root project and use Gradle version 2.0 in the persisted configuration
        IProject rootProject = workspaceOperations.createProject("root-project", rootProjectDir, ImmutableList.of(), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def requestAttributes = new FixedRequestAttributes(rootProjectDir, null, GradleDistribution.forVersion("2.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"), rootProjectDir)
        configurationManager.saveProjectConfiguration(new NullProgressMonitor(), projectConfiguration, rootProject)

        // create child project and use Gradle version 1.0 in the persisted configuration
        File childProjectDir = tempFolder.newFolder()
        IProject childProject = workspaceOperations.createProject("child-project", childProjectDir, ImmutableList.of(), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())
        def childRequestAttributes = new FixedRequestAttributes(rootProjectDir, null, GradleDistribution.forVersion("1.0"), null,
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def childProjectConfiguration = ProjectConfiguration.from(childRequestAttributes, Path.from(":child"), childProjectDir)
        configurationManager.saveProjectConfiguration(new NullProgressMonitor(),childProjectConfiguration, childProject)

        when:
        configurationManager.getRootProjectConfigurations(new NullProgressMonitor())

        then:
        thrown(GradlePluginsRuntimeException)
    }

    def "save and read project with full configuration"() {
        given:
        File projectDir = tempFolder.root
        IProject project = workspaceOperations.createProject("sample-project", projectDir, ImmutableList.of(), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def requestAttributes = new FixedRequestAttributes(projectDir, tempFolder.newFolder(), GradleDistribution.forVersion("1.12"), tempFolder.newFolder(),
                ImmutableList.copyOf("-Xmx256M"), ImmutableList.copyOf("foo"))
        def projectConfiguration = ProjectConfiguration.from(requestAttributes, Path.from(":"), projectDir)

        when:
        configurationManager.saveProjectConfiguration(new NullProgressMonitor(), projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(new NullProgressMonitor(), project) == projectConfiguration
    }

    def "save and read project with minimal configuration"() {
        given:
        File projectDir = tempFolder.newFolder()
        IProject project = workspaceOperations.createProject("sample-project", projectDir, ImmutableList.of(), Arrays.asList(GradleProjectNature.ID), new NullProgressMonitor())

        def attributes = new FixedRequestAttributes(projectDir, null, GradleDistribution.fromBuild(), null,
                ImmutableList.of(), ImmutableList.of())
        def projectConfiguration = ProjectConfiguration.from(attributes, Path.from(":"), projectDir)

        when:
        configurationManager.saveProjectConfiguration(new NullProgressMonitor(), projectConfiguration, project)

        then:
        configurationManager.readProjectConfiguration(new NullProgressMonitor(), project) == projectConfiguration
    }
}
