package org.eclipse.buildship.core.workspace.internal

import spock.lang.Unroll

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistribution

class ImportingProjectCrossVersionTest extends ProjectSynchronizationSpecification {

    @Unroll
    def "Can import a multi-project build with Gradle #distribution.configuration"(GradleDistribution distribution) {
        setup:
        def projectDir = dir('multi-project-build') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
                include 'sub1'
                include 'sub2'
                include 'sub2:subSub1'
            '''

            file 'build.gradle', '''
                description = 'a sample root project'
                task myTask {}
            '''

            dir('sub1') {
                file 'build.gradle', '''
                    description = 'sub project 1'
                    task myFirstTaskOfSub1 {
                        description = '1st task of sub1'
                        group = 'build'
                    }
                   task mySecondTaskOfSub1 {
                       description = '2nd task of sub1'
                   }
                '''
            }
            dir('sub2') {
                file 'build.gradle', '''
                    description = 'sub project 2'
                    task myFirstTaskOfSub2 {
                        description = '1st task of sub2'
                    }
                    task mySecondTaskOfSub2 {
                        description = '2nd task of sub2'
                    }
                    task myTask {
                        description = 'another task of sub2'
                        group = 'build'
                    }

                '''

                dir('subSub1') {
                    file 'build.gradle', '''
                        description = 'subSub project 1 of sub project 2'
                        task myFirstTaskOfSub2subSub1{
                            description = '1st task of sub2:subSub1'
                        }
                        task mySecondTaskOfSub2subSub1{
                            description = '2nd task of sub2:subSub1'
                        }
                        task myTask {}
                    '''
                }
            }
        }


        when:
        importAndWait(projectDir, distribution)

        then:
        allProjects().size() == 4
        numOfGradleErrorMarkers == 0
        findProject('root')
        findProject('sub1')
        findProject('sub2')
        findProject('subSub1')

        where:
        distribution << supportedGradleDistributions
    }

    @Unroll
    def "Gradle #distribution.configuration can import composite builds"(GradleDistribution distribution) {
        setup:
        def projectDir = dir('composite-build') {
            file 'settings.gradle', '''
                rootProject.name='root'
                includeBuild 'included1'
                includeBuild 'included2'
            '''
            dir('included1') {
                dir('sub1')
                dir('sub2')
                file 'settings.gradle', '''
                    rootProject.name = 'included1'
                    include 'sub1', 'sub2'
                '''
            }
            dir('included2') {
                dir('sub1')
                dir('sub2')
                file 'settings.gradle', '''
                    rootProject.name = 'included2'
                    include 'sub1', 'sub2'
                '''
            }
        }

        when:
        // TODO (donat) Buildship doesn't de-duplicate project names which makes the synchronization fail for versions 3.3 <= v < 4.0
        if (higherOrEqual('4.0', distribution) || !higherOrEqual('3.3', distribution)) {
            importAndWait(projectDir, distribution)
        }

        then:
        numOfGradleErrorMarkers == 0
        if (higherOrEqual('4.0', distribution)) {
            assert allProjects().size() == 7
            assert findProject('root')
            assert findProject('included1')
            assert findProject('included2')
            assert findProject('included1-sub1')
            assert findProject('included1-sub2')
            assert findProject('included2-sub1')
            assert findProject('included2-sub2')
        } else if (higherOrEqual('3.3', distribution)) {
            // assert allProjects().size() == 7
            // assert findProject('root')
            // assert findProject('included1')
            // assert findProject('included2')
            // assert findProject('included1-sub1')
            // assert findProject('included1-sub2')
            // assert findProject('included2-sub1')
            // assert findProject('included2-sub2')
        } else {
            assert allProjects().size() == 1
            assert findProject('root')
        }

        where:
        distribution << getSupportedGradleDistributions('>=3.1')
    }
}

