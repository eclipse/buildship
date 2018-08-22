package org.eclipse.buildship.core.internal.configuration.impl

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
        String expectedContent = ['a=av', 'b=bv', 'c=cv', 'eclipse.preferences.version=1'].join(System.lineSeparator) + System.lineSeparator

        when:
        projectPrefs.flush()
        filePrefs.flush()

        then:
        projectPrefsFile.text == expectedContent
        filePrefsFile.text == expectedContent
    }
}
