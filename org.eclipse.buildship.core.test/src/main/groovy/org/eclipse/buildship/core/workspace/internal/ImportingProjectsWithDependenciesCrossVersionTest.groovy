package org.eclipse.buildship.core.workspace.internal

import spock.lang.Unroll

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistribution

class ImportingProjectsWithDependenciesCrossVersionTest extends ProjectSynchronizationSpecification {

    @Unroll
    def "Can import a multi-project build with Gradle #distribution.configuration"(GradleDistribution distribution) {
        setup:
        def projectDir = dir('multi-project-build') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
                include 'api'
                include 'impl'
                include 'sub2:subSub1'
            '''

            file 'build.gradle', '''
                subprojects {
                    apply plugin: 'java'
                    repositories {
                        mavenCentral()
                    }
                }
            '''

            dir('api') {
                file 'build.gradle', '''
                    dependencies {
                        compile 'com.google.guava:guava:18.0'
                    }
                '''
                dir('src/main/java')
            }
            dir('impl') {
                file 'build.gradle', '''
                    dependencies {
                        compile project(':api')
                        compile 'log4j:log4j:1.2.17'
                    }
                '''
                dir('src/main/java')
            }
        }

        when:
        importAndWait(projectDir, distribution)
        IJavaProject apiProject = findJavaProject('api')
        IJavaProject implProject = findJavaProject('impl')
        IClasspathEntry apiProjectDependency = implProject.getResolvedClasspath(true).find { it.entryKind == IClasspathEntry.CPE_PROJECT && it.path.toPortableString() == '/api' }
        IClasspathEntry guavaDependency = apiProject.getResolvedClasspath(true).find { it.entryKind == IClasspathEntry.CPE_LIBRARY && it.path.toPortableString().contains('guava') }

        then:
        apiProjectDependency.exported == higherOrEqual('2.5', distribution) ? false : true

        and:
        apiProjectDependency.accessRules == []
        guavaDependency.accessRules == []

        and:
        guavaDependency.sourceAttachmentPath != null
        guavaDependency.extraAttributes.find { it.name == 'javadoc_location' } == null
        guavaDependency.exported == higherOrEqual('2.5', distribution) ? false : true

        and:
        if (higherOrEqual('4.4', distribution)) {
            assert guavaDependency.extraAttributes.size() == 1
            assert guavaDependency.extraAttributes[0].name == 'gradle_used_by_scope'
            assert guavaDependency.extraAttributes[0].value == 'main,test'
        } else {
            assert guavaDependency.extraAttributes.size() == 0
        }

        where:
        distribution << supportedGradleDistributions
    }
}

