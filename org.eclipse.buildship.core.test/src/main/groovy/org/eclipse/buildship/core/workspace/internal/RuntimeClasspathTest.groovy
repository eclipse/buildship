package org.eclipse.buildship.core.workspace.internal

import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class RuntimeClasspathTest extends ProjectSynchronizationSpecification {

    def "project classpath has entries only from local Gradle classpath container"() {
        setup:
        File location = sampleProject()
        importAndWait(location)

        when:
        IJavaProject javaProject = JavaCore.create(findProject('c'))
        String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject)

        then:
        classpath.find { it.contains('log4j-1.2.17') }
        !classpath.find { it.contains('log4j-1.2.16') }
    }

    private def sampleProject() {
        dir('sample-project') {
            file 'build.gradle',  '''
                subprojects {
                    apply plugin: 'java'

                    repositories {
                        mavenCentral()
                    }
                }

                project(':a') {
                    dependencies {
                        compile 'log4j:log4j:1.2.17'
                    }
                }

                project(':b') {
                    dependencies {
                        compile 'log4j:log4j:1.2.16'
                    }
                }

                project(':c') {
                    dependencies {
                        compile project(':a')
                        compile project(':b')
                        testCompile 'junit:junit:4.12'
                    }
                }
            '''

            file('settings.gradle') << '''
                rootProject.name= 'sample-project'
                include "a"
                include "b"
                include "c"
            '''

            dir('a')
            dir('b')
            dir('c')
        }
    }
}
