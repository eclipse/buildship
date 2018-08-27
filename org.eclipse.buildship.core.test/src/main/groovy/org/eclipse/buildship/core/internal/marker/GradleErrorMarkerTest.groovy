package org.eclipse.buildship.core.internal.marker

import org.gradle.tooling.BuildException

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleErrorMarkerTest extends ProjectSynchronizationSpecification {

    def "Display error marker if synchronization fails"() {
        setup:
        File projectDir = dir('error-marker-test') {
            file 'build.gradle', ''
        }
        importAndWait(projectDir)

        expect:
        numOfGradleErrorMarkers == 0

        when:
        File buildFile = new File(projectDir, 'build.gradle')
        buildFile.text = 'I_AM_ERROR'
        synchronizeAndWait(projectDir)

        then:
        thrown BuildException
        numOfGradleErrorMarkers == 1
    }

    def "Error markers cleaned up after synchronization"() {
        setup:
        File projectDir = dir('error-marker-test') {
            file 'build.gradle', ''
        }
        importAndWait(projectDir)
        File buildFile = new File(projectDir, 'build.gradle')
        buildFile.text = 'I_AM_ERROR'

        when:
        synchronizeAndWait(projectDir)

        then:
        thrown BuildException
        numOfGradleErrorMarkers == 1

        when:
        buildFile.text = ''
        synchronizeAndWait(projectDir)

        then:
        numOfGradleErrorMarkers == 0
    }

    def "Only related markers are updated"() {
        setup:
        File projectDir1 = dir('error-marker-test-1') {
            file 'build.gradle', ''
        }
        File projectDir2 = dir('error-marker-test-2') {
            file 'build.gradle', ''
        }
        importAndWait(projectDir1)
        importAndWait(projectDir2)
        File buildFile1 = new File(projectDir1, 'build.gradle')
        File buildFile2 = new File(projectDir2, 'build.gradle')
        buildFile1.text = 'I_AM_ERROR'
        buildFile2.text = 'I_AM_ERROR'

        when:
        synchronizeAndWait(projectDir1)

        then:
        thrown BuildException
        numOfGradleErrorMarkers == 1

        when:
        synchronizeAndWait(projectDir2)

        then:
        thrown BuildException
        numOfGradleErrorMarkers == 2

        when:
        buildFile1.text = ''
        synchronizeAndWait(projectDir1)

        then:
        numOfGradleErrorMarkers == 1
        findProject('error-marker-test-2').findMember(gradleErrorMarkers[0].resource.projectRelativePath)
    }

    def "Unlinking a project cleans up error markers too"() {
        setup:
        File projectDir = dir('error-marker-test') {
            file 'build.gradle', ''
            file 'settings.gradle', 'include "sub"'
            dir('sub')
        }
        importAndWait(projectDir)
        File buildFile = new File(projectDir, 'build.gradle')
        File settingsFile = new File(projectDir, 'settings.gradle')
        File subBuildFile = new File(projectDir, 'sub/build.gradle')
        subBuildFile.text = 'I_AM_ERROR'

        when:
        synchronizeAndWait(projectDir)

        then:
        thrown BuildException
        numOfGradleErrorMarkers == 1

        when:
        settingsFile.text = ''
        synchronizeAndWait(projectDir)

        then:
        findProject('sub')
        numOfGradleErrorMarkers == 0
    }
}
