package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ProjectNameDeduplicationTest extends ProjectSynchronizationSpecification {

    def "Conflicts are resolved on import"() {
        setup:
        def location = dir('conflict-resolution-on-import') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
                include ':a'
            '''
            file 'build.gradle', ''
            dir('a')
        }
        IProject project = newProject('a')

        when:
        importAndWait(location)

        then:
        findProject("root-a") != null
    }

    def "Conflicts are resolved on synchronization"() {
        setup:
        def location = dir('conflict-resolution-on-sync') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
            '''
            file 'build.gradle', ''
            dir('a')
        }
        IProject project = newProject('a')
        importAndWait(location)

        expect:
        findProject("root-a") == null

        when:
        new File(location, 'settings.gradle') << "include ':a'"
        synchronizeAndWait(location)

        then:
        findProject("root-a") != null
    }

    def "Names from model are deduplicated"() {
        setup:
        def location = dir('conflict-resolution-on-import') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
                include ':a'
            '''
            file 'build.gradle', '''
                project(':a') {
                    apply plugin: 'eclipse'
                    eclipse.project.name='not-a'
                }
            '''
            dir('a')
        }
        IProject project =  newProject('not-a')

        when:
        importAndWait(location)

        then:
        findProject("root-not-a") != null
    }

}
