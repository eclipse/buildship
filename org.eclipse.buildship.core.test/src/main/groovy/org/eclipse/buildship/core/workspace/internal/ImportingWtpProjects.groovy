package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.workspace.WorkspaceOperations

class ImportingWtpProjects extends ProjectSynchronizationSpecification {

    private static final String NON_DEPLOYED = "org.eclipse.jst.component.nondependency"
    private static final String DEPLOYED = "org.eclipse.jst.component.dependency"
    private static final String WTP_COMPONENT_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature";

    def "The eclipseWtp task is run before importing WTP projects"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse'
            """
        }

        WorkspaceOperations operations = Stub(WorkspaceOperations) {
            isNatureRecognizedByEclipse(WTP_COMPONENT_NATURE) >> true
        }
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root)

        then:
        hasComponentDescriptor(root)
        hasFacetDescriptor(root)
    }

    def "The eclipseWtp task is not run if Eclipse WTP is not installed"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse'
            """
        }

        WorkspaceOperations operations = Stub(WorkspaceOperations) {
            isNatureRecognizedByEclipse(WTP_COMPONENT_NATURE) >> false
        }
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root)

        then:
        !hasComponentDescriptor(root)
        !hasFacetDescriptor(root)
    }

    def "The eclipseWtp task is not run if for Gradle < 3.0"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse'
            """
        }

        WorkspaceOperations operations = Stub(WorkspaceOperations) {
            isNatureRecognizedByEclipse(WTP_COMPONENT_NATURE) >> true
        }
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root, GradleDistribution.forVersion('2.13'))

        then:
        !hasComponentDescriptor(root)
        !hasFacetDescriptor(root)
    }

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
        IClasspathEntry dependency = rawClasspath(project).find { it.path == GradleClasspathContainer.CONTAINER_PATH }
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

    def "If only non-deployed dependencies are present, the container is marked as such too"() {
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
        IClasspathEntry dependency = rawClasspath(project).find { it.path == GradleClasspathContainer.CONTAINER_PATH }
        dependency.getExtraAttributes().find { it.name == NON_DEPLOYED }
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

    def "Does not override classpath container customisation"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.AccessRule

                apply plugin: 'eclipse'
                apply plugin: 'war'

                repositories {
                    jcenter()
                }

                dependencies {
                    providedCompile "junit:junit:4.12"
                }

                eclipse {
                    classpath {
                        containers 'org.eclipse.buildship.core.gradleclasspathcontainer'

                        file {
                            whenMerged { classpath ->
                                def container = classpath.entries.find { it.path == 'org.eclipse.buildship.core.gradleclasspathcontainer' }
                                container.exported = true
                                container.entryAttributes.customKey = 'customValue'
                                container.accessRules.add(new AccessRule('1', 'nonAccessibleFilesPattern'))
                            }
                        }
                    }
               }
            """
        }

        when:
        importAndWait(root)

        then:
        def project = findProject('project')
        IClasspathEntry container = rawClasspath(project).find { it.path == GradleClasspathContainer.CONTAINER_PATH }
        container.extraAttributes.length == 2
        container.extraAttributes.find { it.name == NON_DEPLOYED }
        container.extraAttributes.find { it.name == 'customKey' && it.value == 'customValue' }
        container.accessRules.length == 1
        container.accessRules[0].kind == IAccessRule.K_NON_ACCESSIBLE
        container.accessRules[0].pattern.toPortableString() == 'nonAccessibleFilesPattern'
    }

    private IClasspathEntry[] resolvedClasspath(IProject project) {
        JavaCore.create(project).getResolvedClasspath(false)
    }

    private IClasspathEntry[] rawClasspath(IProject project) {
        JavaCore.create(project).rawClasspath
    }

    private hasFacetDescriptor(File root) {
        new File(root, ".settings/org.eclipse.wst.common.project.facet.core.xml").exists()
    }

    private hasComponentDescriptor(File root) {
        new File(root, ".settings/org.eclipse.wst.common.component").exists()
    }

}
