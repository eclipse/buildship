/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf
import spock.lang.Unroll

import org.eclipse.jdt.core.IClasspathEntry

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion

class ImportingProjectsWithDependenciesCrossVersionTest extends ProjectSynchronizationSpecification {

    def sampleProject(GradleDistribution distribution) {
        GradleVersion version = GradleVersion.version(distribution.version)
        def configuration = version <= GradleVersion.version("6.8.2") ? "compile" : "implementation"
        dir('multi-project-build') {
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
                file 'build.gradle', """
                    dependencies {
                        $configuration 'com.google.guava:guava:18.0'
                    }
                """
                dir('src/main/java')
            }
            dir('impl') {
                file 'build.gradle', """
                    dependencies {
                        $configuration project(':api')
                        $configuration 'log4j:log4j:1.2.17'
                    }
                """
                dir('src/main/java')
            }
        }
    }

    @Unroll
    def "Dependencies are not exported for #distribution.version"(GradleDistribution distribution) {
        when:
        importAndWait(sampleProject(distribution), distribution)

        then:
        !apiProjectDependency.exported
        !guavaDependency.exported

        where:
        distribution << getSupportedGradleDistributions('>=2.5')
    }

    @Unroll
    def "Dependenies have no access rules for #distribution.version"(GradleDistribution distribution) {
        when:
        importAndWait(sampleProject(distribution), distribution)

        then:
        apiProjectDependency.accessRules == []
        guavaDependency.accessRules == []

        where:
        distribution << supportedGradleDistributions
    }

    @Unroll
    def "Binary dependencies can define javadoc location for #distribution.version"(GradleDistribution distribution) {
        when:
        importAndWait(sampleProject(distribution), distribution)

        then:
        guavaDependency.sourceAttachmentPath != null
        guavaDependency.extraAttributes.find { it.name == 'javadoc_location' } == null

        where:
        distribution << supportedGradleDistributions
    }

    def "Binary dependencies define classpath scopes for #distribution.version"(GradleDistribution distribution) {
        when:
        importAndWait(sampleProject(distribution), distribution)

        then:
        guavaDependency.extraAttributes.size() == 1
        guavaDependency.extraAttributes[0].name == 'gradle_used_by_scope'
        guavaDependency.extraAttributes[0].value == 'main,test'

        where:
        distribution << getSupportedGradleDistributions('>=4.4')
    }

    @IgnoreIf({ JavaVersion.current().isJava10Compatible() }) // no Gradle versions <4.4 support Java 10 and above
    def "Binary dependencies does not define classpath scopes for #distribution.version"(GradleDistribution distribution) {
        when:
        importAndWait(sampleProject(distribution), distribution, new File(System.getProperty("jdk8.location")))

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

