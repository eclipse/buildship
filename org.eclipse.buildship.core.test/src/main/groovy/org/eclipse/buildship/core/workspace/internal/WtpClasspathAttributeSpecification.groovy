package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer


class WtpClasspathAttributeSpecification extends ProjectSynchronizationSpecification {

    private static final String NON_DEPLOYED = "org.eclipse.jst.component.nondependency"
    private static final String DEPLOYED = "org.eclipse.jst.component.dependency"

    def "No classpath attributes for older Gradle versions"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                repositories.jcenter()
                dependencies {
                    compile "junit:junit:4.12"
                }
            """
        }

        when:
        importAndWait(root, GradleDistribution.forVersion('2.13'))

        then:
        def project = findProject('project')
        IClasspathEntry dependency = resolvedClasspath(project).find { it.path.lastSegment() == 'junit-4.12.jar' }
        IClasspathAttribute[] attributes = dependency.getExtraAttributes()
        attributes.length == 0
    }

    def "No classpath attributes for non-web-projects"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'java'
                repositories.jcenter()
                dependencies {
                    compile "junit:junit:4.12"
                }
            """
        }

        when:
        importAndWait(root, GradleDistribution.forVersion("2.14-20160505000028+0000"))

        then:
        def project = findProject('project')
        IClasspathEntry dependency = resolvedClasspath(project).find { it.path.lastSegment() == 'junit-4.12.jar' }
        IClasspathAttribute[] attributes = dependency.getExtraAttributes()
        attributes.length == 0
    }

    def "Deployed attribute is set for deployed dependencies"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                repositories.jcenter()
                dependencies {
                    compile "junit:junit:4.12"
                }
            """
        }

        when:
        importAndWait(root, GradleDistribution.forVersion("2.14-20160505000028+0000"))

        then:
        def project = findProject('project')
        IClasspathEntry dependency = resolvedClasspath(project).find { it.path.lastSegment() == 'junit-4.12.jar' }
        IClasspathAttribute deploymenAttribute = dependency.getExtraAttributes().find { it.name == DEPLOYED }
        deploymenAttribute.value == "/WEB-INF/lib"
    }

    def "Deployement path is set on container too"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                repositories.jcenter()
                dependencies {
                    compile "junit:junit:4.12"
                }
            """
        }

        when:
        importAndWait(root, GradleDistribution.forVersion("2.14-20160505000028+0000"))

        then:
        def project = findProject('project')
        IClasspathEntry dependency = rawClasspath(project).find { it.path.lastSegment() == GradleClasspathContainer.CONTAINER_ID }
        IClasspathAttribute deploymenAttribute = dependency.getExtraAttributes().find { it.name == DEPLOYED }
        deploymenAttribute.value == "/WEB-INF/lib"
    }

    def "Non-deployed attribute is set for non-deployed dependencies"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                repositories.jcenter()
                dependencies {
                    providedCompile "junit:junit:4.12"
                }
            """
        }

        when:
        importAndWait(root, GradleDistribution.forVersion("2.14-20160505000028+0000"))

        then:
        def project = findProject('project')
        IClasspathEntry dependency = resolvedClasspath(project).find { it.path.lastSegment() == 'junit-4.12.jar' }
        IClasspathAttribute[] attributes = dependency.getExtraAttributes()
        IClasspathAttribute deploymenAttribute = dependency.getExtraAttributes().find { it.name == NON_DEPLOYED }
        deploymenAttribute.value == ""
    }

    def "Does not support mixed deployment paths"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                apply plugin: 'ear'
                repositories.jcenter()
                dependencies {
                    deploy "junit:junit:4.12"
                    earlib "com.google.guava:guava:19.0"
                }
                eclipse.classpath.plusConfigurations += [configurations.deploy, configurations.earlib]
            """
        }
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)

        when:
        importAndWait(root, GradleDistribution.forVersion("2.14-20160505000028+0000"))

        then:
        1 * logger.error(*_)
    }

    private IClasspathEntry[] resolvedClasspath(IProject project) {
        JavaCore.create(project).getResolvedClasspath(false)
    }

    private IClasspathEntry[] rawClasspath(IProject project) {
        JavaCore.create(project).rawClasspath
    }
}
