package org.eclipse.buildship.core.configuration.internal

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

class BuildConfigurationPersistenceTest extends WorkspaceSpecification {

    @Shared
    @Subject
    BuildConfigurationPersistence persistence = new BuildConfigurationPersistence()

    IProject project

    void setup() {
        project = newProject("sample-project")
    }

    def "can't save null as preferences"() {
        when:
        persistence.saveBuildConfiguration(project, null)

        then:
        thrown NullPointerException
    }

    def "can save preferences"() {
        setup:
        persistence.saveBuildConfiguration(project, buildConfigProperties())

        expect:
        def node = new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID)
        node.get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null) == 'GRADLE_DISTRIBUTION(WRAPPER)'
    }

    def "read validates input"() {
        when:
        persistence.readBuildConfiguratonProperties(null)

        then:
        thrown NullPointerException

        // TODO (donat) should we do the validation in the persistence or in the Manager?
        //
        //        when:
        //        project.close(new NullProgressMonitor())
        //        persistence.readBuildConfiguratonProperties(project)
        //
        //        then:
        //        thrown IllegalArgumentException
    }

    def "can read preferences"() {
        setup:
        persistence.saveBuildConfiguration(project, buildConfigProperties())

        when:
        def configuration = persistence.readBuildConfiguratonProperties(project)

        then:
        configuration == buildConfigProperties()
    }

    def "can read preferences even when the preference api is not accessible"() {
        setup:
        fileTree(project.location.toFile()) {
            dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", """
                    connection.gradle.distribution=GRADLE_DISTRIBUTION(WRAPPER)
                    connection.project.dir=
                    eclipse.preferences.version=1
                """ }
        }

        when:
        def configuration = persistence.readBuildConfiguratonProperties(project)

        then:
        configuration == buildConfigProperties()
    }

//    def "delete validates input"() {
//        when:
//        persistence.deleteProjectConfiguration(null)
//
//        then:
//        thrown NullPointerException
//
//        when:
//        project.close(new NullProgressMonitor())
//        persistence.deleteProjectConfiguration(project)
//
//        then:
//        thrown IllegalArgumentException
//    }

//    def "can delete preferences"() {
//        setup:
//        persistence.saveBuildConfiguration(project, buildConfigProperties())
//
//        expect:
//        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null)
//
//        when:
//        persistence.delete(project)
//
//        then:
//        !new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null)
//    }

    @IgnoreIf({!OperatingSystem.current.isWindows()}) // IPath implementation is os-dependent
    def "Windows-style paths can can be read and are replaced with slashes"() {
        setup:
        fileTree(project.location.toFile()) {
            dir('.settings') { file "${CorePlugin.PLUGIN_ID}.prefs", """
                    connection.gradle.distribution=GRADLE_DISTRIBUTION(WRAPPER)
                    connection.project.dir=../..
                    eclipse.preferences.version=1
                """ }
        }

        when:
        def configuration = persistence.readBuildConfiguratonProperties(project)

        then:
        configuration.rootProjectDirectory == project.location.toFile().parentFile.parentFile

        when:
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())
        persistence.saveBuildConfiguration(project, configuration)


        then:
        new File(project.location.toFile(), ".settings/${CorePlugin.PLUGIN_ID}.prefs").text.contains 'connection.project.dir=../..'
    }

    private BuildConfigurationProperties buildConfigProperties() {
        new BuildConfigurationProperties(project.getLocation().toFile(), GradleDistribution.fromBuild(), false, false, false)
    }

}
