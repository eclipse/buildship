package org.eclipse.buildship.core.workspace

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.core.resources.IProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.util.environment.OperatingSystem

@IgnoreIf({ OperatingSystem.current.isWindows() })
class SynchronizeGradleProjectJobWithSymlinks extends ProjectImportSpecification {

    // root
    // |-users
    // | |-name
    // |   |-projects
    // |     |-sample
    // |-projects -> users/name/projects

    def setup() {
        tempFolder.newFolder('users', 'name', 'projects', 'sample')
        Runtime.runtime.exec(['ln', '-s', 'users/name/projects', 'projects'] as String[], null, tempFolder.root)
        assert new File(tempFolder.root, 'projects/sample').canonicalPath.endsWith('users/name/projects/sample')
    }

    def "Can import projects via symlinks"() {
        setup:
        file('users', 'name', 'projects', 'sample' ,'build.gradle')
        file('users', 'name', 'projects', 'sample' ,'settings.gradle') << "rootProject.name ='root'\ninclude 'child'"
        folder('users', 'name', 'projects', 'sample' ,'child')

        when:
        executeProjectImportAndWait(new File(tempFolder.root, 'projects/sample'))

        then:
        findProject('root').location.toOSString() == new File(tempFolder.root, 'users/name/projects/sample').absolutePath
        findProject('child').location.toOSString() == new File(tempFolder.root, 'users/name/projects/sample/child').absolutePath
        settingsFileContent(findProject('root')).contains('"connection_project_dir": ""')
        settingsFileContent(findProject('child')).contains('"connection_project_dir": ".."')
    }

    private String settingsFileContent(IProject project) {
        project.getFile('.settings/gradle.prefs').location.toFile().text
    }

}
