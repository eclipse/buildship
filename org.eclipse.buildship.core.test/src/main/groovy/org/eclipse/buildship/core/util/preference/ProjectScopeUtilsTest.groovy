package org.eclipse.buildship.core.util.preference

import java.util.Map;

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ProjectScope
import org.eclipse.core.runtime.NullProgressMonitor

class ProjectScopeUtilsTest extends Specification {

    @Rule
    TemporaryFolder projectDir

    IProject project

    def setup() {
        project = EclipseProjects.newProject("sample-project", projectDir.root)
    }

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "store validates input"() {
        when:
        ProjectScopeUtils.store(null, 'pref-node', ['key':'value'])

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.store(project, null, ['key':'value'])

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.store(project, 'pref-node', null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        ProjectScopeUtils.store(project, 'pref-node', ['key':'value'])

        then:
        thrown IllegalArgumentException
    }

    def "can store preferences"() {
        setup:
        ProjectScopeUtils.store(project, 'pref-node', ['key1':'value1', 'key2':'value2'])

        expect:
        new ProjectScope(project).getNode('pref-node').keys().length == 2
        new ProjectScope(project).getNode('pref-node').get('key1', null) == 'value1'
        new ProjectScope(project).getNode('pref-node').get('key2', null) == 'value2'
    }

    def "store empty preferences does nothing" () {
        setup:
        IProject mockedProject = Mock(IProject)
        mockedProject.isAccessible() >> true

        expect:
        ProjectScopeUtils.store(mockedProject, 'pref-node', [:])
    }

    // load()

    def "load validates input"() {
        when:
        ProjectScopeUtils.load(null, 'pref-node', ['key'] as Set)

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.load(project, null, ['key'] as Set)

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.load(project, 'pref-node', null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        ProjectScopeUtils.load(project, 'pref-node', ['key'] as Set)

        then:
        thrown IllegalArgumentException
    }

    def "can load preferenes"() {
        setup:
        def node = new ProjectScope(project).getNode('pref-node')
        node.put('key1', 'value1')
        node.put('key2', 'value2')
        node.flush()

        expect:
        ProjectScopeUtils.load(project, 'pref-node', ['key1', 'key2'] as Set) == ['key1':'value1', 'key2':'value2']
    }

    def "load empty key set returns empty list without interacting any apis" () {
        setup:
        IProject mockedProject = Mock(IProject)
        mockedProject.isAccessible() >> true

        expect:
        ProjectScopeUtils.load(mockedProject, 'pref-node', [] as Set) == [:]
    }

    def "can read preferences even the preference api is not accessible"() {
        setup:
        project.close(new NullProgressMonitor())
        projectDir.newFolder('.settings')
        projectDir.newFile('.settings/pref-node.prefs') << "key1=value1\nkey2=value2"
        project.open(IResource.BACKGROUND_REFRESH, ,new NullProgressMonitor())

        ProjectScopeUtils.load(project, 'pref-node', ['key1', 'key2'] as Set) == ['key1':'value1', 'key2':'value2']
    }

    def "if not all preference keys are present, then an exception is thrown"() {
        setup:
        def node = new ProjectScope(project).getNode('pref-node')
        node.put('key2', 'value2')
        node.flush()

        when:
        ProjectScopeUtils.load(project, 'pref-node', ['key1', 'key2'] as Set)

        then:
        thrown GradlePluginsRuntimeException
    }

    def "delete validates input"() {
        when:
        ProjectScopeUtils.delete(null, 'pref-node', ['key'] as Set)

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.delete(project, null, ['key'] as Set)

        then:
        thrown NullPointerException

        when:
        ProjectScopeUtils.delete(project, 'pref-node', null)

        then:
        thrown NullPointerException

        when:
        project.close(new NullProgressMonitor())
        ProjectScopeUtils.delete(project, 'pref-node', ['key'] as Set)

        then:
        thrown IllegalArgumentException
    }

    def "can delete preferences"() {
        setup:
        def node = new ProjectScope(project).getNode('pref-node')
        node.put('key1', 'value1')
        node.put('key2', 'value2')
        node.flush()

        when:
        ProjectScopeUtils.delete(project, 'pref-node', ['key1'] as Set)

        then:
        new ProjectScope(project).getNode('pref-node').keys().length == 1
        new ProjectScope(project).getNode('pref-node').get('key2', null) == 'value2'
    }

    def "delete empty preferences does nothing" () {
        setup:
        IProject mockedProject = Mock(IProject)
        mockedProject.isAccessible() >> true

        expect:
        ProjectScopeUtils.delete(mockedProject, 'pref-node', [] as Set)
    }
}
