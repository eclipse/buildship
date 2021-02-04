/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import spock.lang.Shared
import spock.lang.Subject

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class BuildConfigurationPersistenceTest extends WorkspaceSpecification {

    @Shared
    @Subject
    BuildConfigurationPersistence persistence = new BuildConfigurationPersistence()

    IProject project
    File projectDir

    def setup() {
        project = newProject("sample-project")
        projectDir = dir("external")
    }

    def "save validates input"() {
        when:
        persistence.saveBuildConfiguration(project, null)

        then:
        thrown NullPointerException

        when:
        persistence.saveBuildConfiguration((File) null, validProperties(projectDir))

        then:
        thrown NullPointerException

        when:
        persistence.saveBuildConfiguration(projectDir, null)

        then:
        thrown NullPointerException

        when:
        persistence.saveBuildConfiguration((IProject) null, validProperties(project))

        then:
        thrown NullPointerException
    }

    def "can save and read preferences for workspace project"() {
        setup:
        BuildConfigurationProperties properties = validProperties(project)
        persistence.saveBuildConfiguration(project, properties)

        expect:
        persistence.readBuildConfiguratonProperties(project) == properties
    }

    def "can save and read preferences for external project"() {
        setup:
        BuildConfigurationProperties properties = validProperties(projectDir)
        persistence.saveBuildConfiguration(projectDir, properties)

        expect:
        persistence.readBuildConfiguratonProperties(projectDir) == properties
    }

    def "reading build configuration validates input"() {
        when:
        persistence.readBuildConfiguratonProperties((IProject) null)

        then:
        thrown NullPointerException

        when:
        persistence.readBuildConfiguratonProperties((File) null)

        then:
        thrown NullPointerException
    }

    def "Reading nonexisting build configuration returns default"() {
        when:
        BuildConfigurationProperties properties = persistence.readBuildConfiguratonProperties(project)

        then:
        properties.rootProjectDirectory == project.location.toFile()
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false
        properties.autoSync == false

        when:
        properties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        properties.rootProjectDirectory == projectDir.canonicalFile
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false
        properties.autoSync == false
    }

    def "Reading broken build configuration results in using default settings"() {
        setup:
        String prefsFileContent = """override.workspace.settings=not_true_nor_false
connection.gradle.distribution=MODIFIED_DISTRIBUTION"""
        fileTree(project.location.toFile()) { dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", prefsFileContent } }
        fileTree(projectDir) { dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", prefsFileContent } }
        project.refreshLocal(IResource.DEPTH_INFINITE, null)

        when:
        BuildConfigurationProperties properties = persistence.readBuildConfiguratonProperties(project)

        then:
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false
        properties.autoSync == false

        when:
        properties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false
        properties.autoSync == false
    }


    def "Configuration properties are persisted"(boolean override, GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode, boolean autoSync, boolean showConsole, boolean showExecutions) {
        setup:
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        File javaHome = dir('java-home').canonicalFile
        List<String> arguments = ['--info']
        List<String> jvmArguments = ['-Dfoo=bar']
        BuildConfigurationProperties properties = new BuildConfigurationProperties(projectDir, distribution, gradleUserHome, javaHome, override, buildScansEnabled, offlineMode, autoSync, arguments, jvmArguments, showConsole, showExecutions)
        persistence.saveBuildConfiguration(project, properties)
        persistence.saveBuildConfiguration(projectDir, properties)

        when:
        BuildConfigurationProperties importedProjectProperties = persistence.readBuildConfiguratonProperties(project)
        BuildConfigurationProperties externalProjectProperties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        importedProjectProperties.overrideWorkspaceSettings == override
        importedProjectProperties.gradleDistribution == distribution
        importedProjectProperties.gradleUserHome == gradleUserHome
        importedProjectProperties.javaHome == javaHome
        importedProjectProperties.buildScansEnabled == buildScansEnabled
        importedProjectProperties.offlineMode == offlineMode
        importedProjectProperties.autoSync == autoSync
        importedProjectProperties.arguments == arguments
        importedProjectProperties.jvmArguments == jvmArguments
        importedProjectProperties.showConsoleView == showConsole
        importedProjectProperties.showExecutionsView == showExecutions

        externalProjectProperties.overrideWorkspaceSettings == override
        externalProjectProperties.gradleDistribution == distribution
        externalProjectProperties.gradleUserHome == gradleUserHome
        externalProjectProperties.javaHome == javaHome
        externalProjectProperties.buildScansEnabled == buildScansEnabled
        externalProjectProperties.offlineMode == offlineMode
        externalProjectProperties.autoSync == autoSync
        externalProjectProperties.arguments == arguments
        externalProjectProperties.jvmArguments == jvmArguments
        externalProjectProperties.showConsoleView == showConsole
        externalProjectProperties.showExecutionsView == showExecutions

        where:
        override | distribution                         | buildScansEnabled | offlineMode | autoSync | showConsole | showExecutions
        false    | GradleDistribution.forVersion('3.5') | false             | false       | true     | false       | true
        true     | GradleDistribution.forVersion('3.4') | false             | true        | false    | true        | false
        false    | GradleDistribution.forVersion('3.3') | true              | false       | false    | true        | false
        true     | GradleDistribution.forVersion('3.2') | true              | true        | true     | false       | true
    }

    def "pathToRoot methods validate input"() {
        when:
        persistence.readPathToRoot(null)

        then:
        thrown NullPointerException

        when:
        persistence.savePathToRoot(project, null)

        then:
        thrown NullPointerException

        when:
        persistence.savePathToRoot(projectDir, null)

        then:
        thrown NullPointerException

        when:
        persistence.savePathToRoot((IProject) null, '.')

        then:
        thrown NullPointerException

        when:
        persistence.savePathToRoot((File) null, '.')

        then:
        thrown NullPointerException

        when:
        persistence.deletePathToRoot((IProject) null)

        then:
        thrown NullPointerException

        when:
        persistence.deletePathToRoot((File) null)

        then:
        thrown NullPointerException
    }

    def "reading nonexisting path to root results in runtime exception"() {
        when:
        persistence.readPathToRoot(project)

        then:
        thrown RuntimeException

        when:
        persistence.readPathToRoot(projectDir)

        then:
        thrown RuntimeException
    }

    def "can read and save path to root on workspace project"() {
        when:
        persistence.savePathToRoot(project, 'path-to-root')

        then:
        persistence.readPathToRoot(project) == 'path-to-root'
    }

    def "can read and save path to root on external project"() {
        when:
        persistence.savePathToRoot(projectDir, 'path-to-root')

        then:
        persistence.readPathToRoot(projectDir) == 'path-to-root'
    }

    def "can delete path to root on workspace project"() {
        setup:
        persistence.savePathToRoot(project, 'path-to-root')
        persistence.deletePathToRoot(project)

        when:
        persistence.readPathToRoot(project)

        then:
        thrown RuntimeException
    }

    def "can delete path to root on external project"() {
        setup:
        persistence.savePathToRoot(projectDir, 'path-to-root')
        persistence.deletePathToRoot(projectDir)

        when:
        persistence.readPathToRoot(projectDir)

        then:
        thrown RuntimeException
    }

    private BuildConfigurationProperties validProperties(IProject project) {
        new BuildConfigurationProperties(project.getLocation().toFile(), GradleDistribution.fromBuild(), null, null, false, false, false, false, [], [], false, false)
    }

    private BuildConfigurationProperties validProperties(File projectDir) {
        new BuildConfigurationProperties(projectDir, GradleDistribution.fromBuild(), null, null, false, false, false, false, [], [], false, false)
    }
}
