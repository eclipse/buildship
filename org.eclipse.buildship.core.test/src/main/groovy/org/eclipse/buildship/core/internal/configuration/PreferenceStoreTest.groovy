/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class PreferenceStoreTest extends WorkspaceSpecification {

    def "PreferenceStore implementations produce identical content with sorted entries and without timestamp"() {
        setup:
        IProject project = newProject('test-preference-store')
        File filePrefsFile = file('test.prefs')
        PreferenceStore projectPrefs = PreferenceStore.forProjectScope(project, 'test')
        PreferenceStore filePrefs= PreferenceStore.forPreferenceFile(filePrefsFile)
        File projectPrefsFile = project.getFile('.settings/test.prefs').getLocation().toFile()
        ['c', 'a', 'b'].each {
            projectPrefs.write(it, "${it}v")
            filePrefs.write(it, "${it}v")
        }
        def separator = System.getProperty('line.separator', '/')
        String expectedContent = ['a=av', 'b=bv', 'c=cv', 'eclipse.preferences.version=1'].join(separator) + separator

        when:
        projectPrefs.flush()
        filePrefs.flush()

        then:
        projectPrefsFile.text == expectedContent
        filePrefsFile.text == expectedContent
    }
}
