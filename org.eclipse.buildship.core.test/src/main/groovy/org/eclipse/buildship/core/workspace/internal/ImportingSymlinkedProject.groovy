package org.eclipse.buildship.core.workspace.internal

import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.util.environment.OperatingSystem

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.internal.DefaultProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

@IgnoreIf({OperatingSystem.current.isWindows()})
class ImportingSymlinkedProject extends ProjectImportSpecification {

    /*
     * root
     * |-users
     * | |-name
     * |   |-projects
     * |     |-sample
     * |-projects -> users/name/projects
     */
    def setup() {
        tempFolder.newFolder('users', 'name', 'projects', 'sample')
        def ln = Runtime.runtime.exec([
            'ln',
            '-s',
            'users/name/projects',
            'projects'] as String[], null, tempFolder.root)
        ln.waitFor()
        assert new File(tempFolder.root, 'projects/sample').canonicalPath.endsWith('users/name/projects/sample')
    }

    def "Can import projects in symlinked locations"() {
        setup:
        file('users', 'name', 'projects', 'sample' ,'build.gradle')
        file('users', 'name', 'projects', 'sample' ,'settings.gradle') << "rootProject.name ='root'\ninclude 'child'"
        folder('users', 'name', 'projects', 'sample' ,'child')

        when:
        executeProjectImportAndWait(new File(tempFolder.root, 'projects/sample'))

        then:
        IProject root = findProject("root")
        IProject child = findProject("child")
        root.location.toFile() == new File(tempFolder.root, 'projects/sample').canonicalFile
        child.location.toFile() == new File(tempFolder.root, 'projects/sample/child').canonicalFile
        new ProjectScope(root).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ""
        new ProjectScope(child).getNode(CorePlugin.PLUGIN_ID).get(DefaultProjectConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ".."
    }
}