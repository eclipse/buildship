package org.eclipse.buildship.core.configuration.internal

import spock.lang.Shared
import spock.lang.Subject

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

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
        DefaultBuildConfigurationProperties properties = validProperties(project)
        persistence.saveBuildConfiguration(project, properties)

        expect:
        persistence.readBuildConfiguratonProperties(project) == properties
    }

    def "can save and read preferences for external project"() {
        setup:
        DefaultBuildConfigurationProperties properties = validProperties(projectDir)
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
        DefaultBuildConfigurationProperties properties = persistence.readBuildConfiguratonProperties(project)

        then:
        properties.rootProjectDirectory == project.location.toFile()
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false

        when:
        properties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        properties.rootProjectDirectory == projectDir.canonicalFile
        properties.overrideWorkspaceSettings == false
        properties.gradleDistribution == GradleDistribution.fromBuild()
        properties.gradleUserHome == null
        properties.buildScansEnabled == false
        properties.offlineMode == false
    }

    def "Reading broken build configuration results in runtime exception"() {
        setup:
        String prefsFileContent = """override.workspace.settings=true
connection.gradle.distribution=INVALID_GRADLE_DISTRO"""
        fileTree(project.location.toFile()) { dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", prefsFileContent } }
        fileTree(projectDir) { dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", prefsFileContent } }
        project.refreshLocal(IResource.DEPTH_INFINITE, null)

        when:
        persistence.readBuildConfiguratonProperties(project)

        then:
        thrown RuntimeException

        when:
        persistence.readBuildConfiguratonProperties(projectDir)

        then:
        thrown RuntimeException
    }

    def "If workspace override is not set then overridden configuration properties are ignored"() {
        setup:
        DefaultBuildConfigurationProperties properties = new DefaultBuildConfigurationProperties(projectDir, GradleDistribution.forVersion('2.0'), dir('gradle-user-home'), false, true, true)
        persistence.saveBuildConfiguration(project, properties)
        persistence.saveBuildConfiguration(projectDir, properties)

        when:
        DefaultBuildConfigurationProperties importedProjectProperties = persistence.readBuildConfiguratonProperties(project)
        DefaultBuildConfigurationProperties externalProjectProperties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        importedProjectProperties.overrideWorkspaceSettings == false
        importedProjectProperties.gradleDistribution == GradleDistribution.fromBuild()
        importedProjectProperties.gradleUserHome == null
        importedProjectProperties.buildScansEnabled == false
        importedProjectProperties.offlineMode == false

        externalProjectProperties.overrideWorkspaceSettings == false
        externalProjectProperties.gradleDistribution == GradleDistribution.fromBuild()
        externalProjectProperties.gradleUserHome == null
        externalProjectProperties.buildScansEnabled == false
        externalProjectProperties.offlineMode == false
    }

    def "If workspace override is set then overridden configuration properties are persisted"(GradleDistribution distribution, boolean buildScansEnabled, boolean offlineMode) {
        setup:
        File gradleUserHome = dir('gradle-user-home').canonicalFile
        DefaultBuildConfigurationProperties properties = new DefaultBuildConfigurationProperties(projectDir, distribution, gradleUserHome, true, buildScansEnabled, offlineMode)
        persistence.saveBuildConfiguration(project, properties)
        persistence.saveBuildConfiguration(projectDir, properties)

        when:
        DefaultBuildConfigurationProperties importedProjectProperties = persistence.readBuildConfiguratonProperties(project)
        DefaultBuildConfigurationProperties externalProjectProperties = persistence.readBuildConfiguratonProperties(projectDir)

        then:
        importedProjectProperties.overrideWorkspaceSettings == true
        importedProjectProperties.gradleDistribution == distribution
        importedProjectProperties.gradleUserHome == gradleUserHome
        importedProjectProperties.buildScansEnabled == buildScansEnabled
        importedProjectProperties.offlineMode == offlineMode

        externalProjectProperties.overrideWorkspaceSettings == true
        externalProjectProperties.gradleDistribution == distribution
        externalProjectProperties.gradleUserHome == gradleUserHome
        externalProjectProperties.buildScansEnabled == buildScansEnabled
        externalProjectProperties.offlineMode == offlineMode

        where:
        distribution                         | buildScansEnabled | offlineMode
        GradleDistribution.forVersion('3.5') | false             | false
        GradleDistribution.forVersion('3.4') | false             | true
        GradleDistribution.forVersion('3.3') | true              | false
        GradleDistribution.forVersion('3.2') | true              | true
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

    private DefaultBuildConfigurationProperties validProperties(IProject project) {
        new DefaultBuildConfigurationProperties(project.getLocation().toFile(), GradleDistribution.fromBuild(), null, false, false, false)
    }

    private DefaultBuildConfigurationProperties validProperties(File projectDir) {
        new DefaultBuildConfigurationProperties(projectDir, GradleDistribution.fromBuild(), null, false, false, false)
    }
}
