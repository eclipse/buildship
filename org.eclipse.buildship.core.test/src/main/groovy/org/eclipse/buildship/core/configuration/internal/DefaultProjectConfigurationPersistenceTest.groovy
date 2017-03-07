package org.eclipse.buildship.core.configuration.internal

import javax.jws.Oneway

import groovy.lang.Closure
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Subject
import spock.util.environment.OperatingSystem

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

class DefaultProjectConfigurationPersistenceTest extends WorkspaceSpecification {

    @Shared
    @Subject
    DefaultProjectConfigurationPersistence persistence = new DefaultProjectConfigurationPersistence()

    IProject project

   void setup() {
        project = newProject("sample-project")
    }

    def "save validates input"() {
        when:
        persistence.saveProjectConfiguration(null, project)

        then:
        thrown NullPointerException

        when:
        persistence.saveProjectConfiguration(projectConfiguration(), null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        persistence.saveProjectConfiguration(projectConfiguration(), project)

        then:
        thrown IllegalArgumentException
    }

    def "can save preferences"() {
        setup:
        persistence.saveProjectConfiguration(projectConfiguration(), project)

        expect:
        def node = new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID)
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ''
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null) == 'GRADLE_DISTRIBUTION(WRAPPER)'
    }

    def "read validates input"() {
        when:
        persistence.readProjectConfiguration(null)

        then:
        thrown NullPointerException
    }

    def "can read preferences"() {
        setup:
        persistence.saveProjectConfiguration(projectConfiguration(), project)

        when:
        def configuration = persistence.readProjectConfiguration(project)

        then:
        configuration == projectConfiguration()
    }

    def "can read preferences even when the preference api is not accessible"() {
        setup:
        fileTree(project.location.toFile()) {
            dir('.settings') {
                file "${CorePlugin.PLUGIN_ID}.prefs", """
                    connection.gradle.distribution=GRADLE_DISTRIBUTION(WRAPPER)
                    connection.project.dir=
                    eclipse.preferences.version=1
                """
            }
        }

        when:
        def configuration = persistence.readProjectConfiguration(project)

        then:
        configuration == projectConfiguration()
    }

    def "delete validates input"() {
        when:
        persistence.deleteProjectConfiguration(null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        persistence.deleteProjectConfiguration(project)

        then:
        thrown IllegalArgumentException
    }

    def "can delete preferences"() {
        setup:
        persistence.saveProjectConfiguration(projectConfiguration(), project)

        expect:
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null)

        when:
        persistence.deleteProjectConfiguration(project)

        then:
        !new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null)
    }

    @IgnoreIf({!OperatingSystem.current.isWindows()}) // IPath implementation is os-dependent
    def "Windows-style paths can can be read and are replaced with slashes"() {
        setup:
        fileTree(project.location.toFile()) {
            dir('.settings') {
                file "${CorePlugin.PLUGIN_ID}.prefs", """
                    connection.gradle.distribution=GRADLE_DISTRIBUTION(WRAPPER)
                    connection.project.dir=..\\\\..
                    eclipse.preferences.version=1
                """
            }
        }

        when:
        def configuration = persistence.readProjectConfiguration(project)

        then:
        configuration.rootProjectDirectory == project.location.toFile().parentFile.parentFile

        when:
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())
        persistence.saveProjectConfiguration(configuration, project)


        then:
        new File(project.location.toFile(), ".settings/${CorePlugin.PLUGIN_ID}.prefs").text.contains 'connection.project.dir=../..'
    }

    private ProjectConfiguration projectConfiguration() {
        ProjectConfiguration.fromWorkspaceConfig(
            project.location.toFile(),
            GradleDistribution.fromBuild()
        )
    }

}
