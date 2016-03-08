package org.eclipse.buildship.core.configuration.internal

import java.util.Map

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared;
import spock.lang.Specification
import spock.lang.Subject;

import com.google.common.collect.ImmutableList

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException
import org.eclipse.buildship.core.configuration.ProjectConfiguration
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.NullProgressMonitor

class DefaultProjectConfigurationPersistenceTest extends WorkspaceSpecification {

    @Shared
    @Subject
    DefaultProjectConfigurationPersistence persistence = new DefaultProjectConfigurationPersistence()

    IProject project

    def setup() {
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
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_PROJECT_PATH, null) == ':'
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ''
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_USER_HOME, null) == 'null'
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_GRADLE_DISTRIBUTION, null) == 'GRADLE_DISTRIBUTION(WRAPPER)'
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_JAVA_HOME, null) == 'null'
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_JVM_ARGUMENTS, null) == ''
        node.get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_ARGUMENTS, null) == ''
    }

    def "read validates input"() {
        when:
        persistence.readProjectConfiguration(null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        persistence.readProjectConfiguration(project)

        then:
        thrown IllegalArgumentException
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
                    connection.arguments=
                    connection.gradle.distribution=GRADLE_DISTRIBUTION(WRAPPER)
                    connection.gradle.user.home=null
                    connection.java.home=null
                    connection.jvm.arguments=
                    connection.project.dir=
                    eclipse.preferences.version=1
                    project.path=\\:
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
        new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_PROJECT_PATH, null)

        when:
        persistence.deleteProjectConfiguration(project)

        then:
        !new ProjectScope(project).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_PROJECT_PATH, null)

    }

    private ProjectConfiguration projectConfiguration() {
        ProjectConfiguration.from(
            new FixedRequestAttributes(project.location.toFile(),
                    null,
                    GradleDistribution.fromBuild(),
                    null,
                    ImmutableList.of(),
                    ImmutableList.of()),
            Path.from(':'))
    }

}
