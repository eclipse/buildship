package org.eclipse.buildship.core.workspace.internal

import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.util.environment.OperatingSystem

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.internal.BuildConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;

@IgnoreIf({OperatingSystem.current.isWindows()})
class ImportingSymlinkedProject extends ProjectSynchronizationSpecification {

    /*
     * root
     * |-users
     * | |-name
     * |   |-projects
     * |     |-sample
     * |-projects -> users/name/projects
     */
    def setup() {
        dir('users/name/projects/sample')
        def ln = Runtime.runtime.exec(['ln', '-s', 'users/name/projects', 'projects'] as String[], null, testDir)
        ln.waitFor()
        assert new File(testDir, 'projects/sample').canonicalPath.endsWith('users/name/projects/sample')
    }

    def "Can import projects in symlinked locations"() {
        setup:
        def childDir
        def rootDir = dir('users/name/projects/sample') {
            file 'settings.gradle', "rootProject.name ='root'\ninclude 'child'"
            childDir = dir 'child'
        }
        when:
        importAndWait(rootDir)

        then:
        IProject root = findProject("root")
        IProject child = findProject("child")
        root.location.toFile() == new File(testDir, 'projects/sample').canonicalFile
        child.location.toFile() == new File(testDir, 'projects/sample/child').canonicalFile
        new BuildConfigurationPersistence().readPathToRoot(rootDir) == ''
        new BuildConfigurationPersistence().readPathToRoot(childDir) == '..'
    }
}