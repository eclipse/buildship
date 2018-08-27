package org.eclipse.buildship.core.internal.workspace

import spock.lang.Unroll

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution

class ImportingProjectsWithDependenciesCrossVersionTest extends ProjectSynchronizationSpecification {

    File projectDir

    def setup() {
        projectDir = dir('multi-project-build') {
            file 'settings.gradle', '''
                rootProject.name = 'root'
                include 'api'
                include 'impl'
                include 'sub2:subSub1'
            '''

            file 'build.gradle', """
                subprojects {
                    apply plugin: 'java'
                    ${jcenterRepositoryBlock}
                }
            """

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
    }

    @Unroll
    def "Dependencies are not exported for #distribution.configuration"(GradleDistribution distribution) {
        when:
        importAndWait(projectDir, distribution)

        then:
        !apiProjectDependency.exported
        !guavaDependency.exported

        where:
        distribution << getSupportedGradleDistributions('>=2.5')
    }

    @Unroll
    def "Dependenies have no access rules for #distribution.configuration"(GradleDistribution distribution) {
        when:
        importAndWait(projectDir, distribution)

        then:
        apiProjectDependency.accessRules == []
        guavaDependency.accessRules == []

        where:
        distribution << supportedGradleDistributions
    }

    @Unroll
    def "Binary dependencies can define javadoc location for #distribution.configuration"(GradleDistribution distribution) {
        when:
        importAndWait(projectDir, distribution)

        then:
        guavaDependency.sourceAttachmentPath != null
        guavaDependency.extraAttributes.find { it.name == 'javadoc_location' } == null

        where:
        distribution << supportedGradleDistributions
    }

    def "Binary dependencies define classpath scopes for #distribution.configuration"(GradleDistribution distribution) {
        when:
        importAndWait(projectDir, distribution)

        then:
        guavaDependency.extraAttributes.size() == 1
        guavaDependency.extraAttributes[0].name == 'gradle_used_by_scope'
        guavaDependency.extraAttributes[0].value == 'main,test'

        where:
        distribution << getSupportedGradleDistributions('>=4.4')
    }

    def "Binary dependencies does not define classpath scopes for #distribution.configuration"(GradleDistribution distribution) {
        when:
        importAndWait(projectDir, distribution)

        then:
        guavaDependency.extraAttributes.length == 0

        where:
        distribution << getSupportedGradleDistributions('<4.4')
    }

    private IClasspathEntry getApiProjectDependency() {
        findJavaProject('impl').getResolvedClasspath(true).find { it.entryKind == IClasspathEntry.CPE_PROJECT && it.path.toPortableString() == '/api' }
    }

    private IClasspathEntry getGuavaDependency() {
        findJavaProject('api').getResolvedClasspath(true).find { it.entryKind == IClasspathEntry.CPE_LIBRARY && it.path.toPortableString().contains('guava') }
    }
}

