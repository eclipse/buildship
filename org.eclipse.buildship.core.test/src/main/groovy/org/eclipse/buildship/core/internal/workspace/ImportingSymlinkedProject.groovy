/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import spock.lang.Ignore

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ProjectScope

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfigurationPersistence
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification;

@Ignore // see https://github.com/eclipse/buildship/issues/661
//@IgnoreIf({OperatingSystem.current.isWindows()})
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
        def rootDir = dir('users/name/projects/sample') {
            file 'settings.gradle', "rootProject.name ='root'\ninclude 'child'"
            dir 'child'
        }
        when:
        importAndWait(rootDir)

        then:
        IProject root = findProject("root")
        IProject child = findProject("child")
        root.location.toFile() == new File(testDir, 'projects/sample').canonicalFile
        child.location.toFile() == new File(testDir, 'projects/sample/child').canonicalFile
        new ProjectScope(root).getNode(CorePlugin.PLUGIN_ID).get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ""
        new ProjectScope(child).getNode(CorePlugin.PLUGIN_ID).get(BuildConfigurationPersistence.PREF_KEY_CONNECTION_PROJECT_DIR, null) == ".."
    }
}
