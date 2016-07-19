package org.eclipse.buildship.core.workspace.internal

import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.IRuntimeClasspathEntry
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class RuntimeClasspathTest extends ProjectSynchronizationSpecification {

    File location
    File buildFile

    void setup() {
        location = dir('sample-project') {
            file('settings.gradle') << "include 'a', 'b', 'c'"
            dir('a'); dir('b'); dir('c')
            buildFile = file 'build.gradle', '''
                subprojects {
                    apply plugin: 'java'

                    repositories {
                        mavenCentral()
                    }
                }
            '''
        }
    }

    def "Project classpath has entries only from local Gradle classpath container"() {
        setup:
        buildFile << '''
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
        importAndWait(location)

        when:
        IJavaProject javaProject = JavaCore.create(findProject('c'))
        IRuntimeClasspathEntry[] classpath = projectRuntimeClasspath(javaProject)

        then:
        classpath.find { it.path.lastSegment() == 'log4j-1.2.17.jar' }
        !classpath.find { it.path.lastSegment() == 'log4j-1.2.16.jar' }
    }

    def "Dependencies of project dependencies are removed from the runtime classpath"() {
        setup:
        buildFile << '''
            project(':a') {
                dependencies {
                    compile 'org.springframework:spring-core:4.3.1.RELEASE'
                }
            }

            project(':b') {
                configurations.all {
                    exclude group: 'commons-logging'
                }

                dependencies {
                    compile project(':a')
                    compile 'org.springframework:spring-core:4.3.1.RELEASE'
                }
            }
        '''
        importAndWait(location)

        when:
        IJavaProject javaProject = JavaCore.create(findProject('b'))
        IRuntimeClasspathEntry[] classpath = projectRuntimeClasspath(javaProject)

        then:
        !classpath.find { it.path.toPortableString().contains('commons-logging') }
    }

    def "Dependencies are still on the runtime classpath"() {
        setup:
        new File(location, 'b/lib').mkdirs()
        buildFile << '''
            project(':b') {
                dependencies {
                    compile project(':a')
                    compile 'log4j:log4j:1.2.17'
                    compile files('lib')
                }
            }
        '''
        importAndWait(location)

        when:
        IJavaProject javaProject = JavaCore.create(findProject('b'))
        IRuntimeClasspathEntry[] classpath = projectRuntimeClasspath(javaProject)

        then:
        classpath.length == 3
        classpath.find { it.type == IRuntimeClasspathEntry.PROJECT && it.path.lastSegment() == 'a' }
        classpath.find { it.type == IRuntimeClasspathEntry.ARCHIVE && it.path.lastSegment() == 'log4j-1.2.17.jar' }
        classpath.find { it.type == IRuntimeClasspathEntry.ARCHIVE && it.path.lastSegment() == 'lib' }

    }

    private IRuntimeClasspathEntry[] projectRuntimeClasspath(IJavaProject project) {
        IRuntimeClasspathEntry projectEntry = JavaRuntime.computeUnresolvedRuntimeClasspath(project).find { it.path == project.project.fullPath }
        JavaRuntime.resolveRuntimeClasspathEntry(projectEntry, project)
    }
}
