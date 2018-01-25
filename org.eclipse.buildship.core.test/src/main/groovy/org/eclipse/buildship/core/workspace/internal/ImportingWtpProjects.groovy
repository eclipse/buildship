package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf
import spock.lang.Issue

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.UnsupportedConfigurationException
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.gradle.GradleDistribution
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

        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: true)
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

        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: false)
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

        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: true)
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root, GradleDistribution.forVersion('2.13'))

        then:
        !hasComponentDescriptor(root)
        !hasFacetDescriptor(root)
    }

    def "No error if eclipseWtp task is not found"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse.project.natures 'org.eclipse.wst.common.modulecore.ModuleCoreNature'
            """
        }

        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: true)
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root)

        then:
        findProject("project")
        !hasComponentDescriptor(root)
        !hasFacetDescriptor(root)
    }

    @IgnoreIf({ JavaVersion.current().java9Compatible })
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
        importAndWait(root)

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
        importAndWait(root)

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
        importAndWait(root)

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
        importAndWait(root)

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
        importAndWait(root)

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
        importAndWait(root)

        then:
        thrown(UnsupportedConfigurationException)
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

    @Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=506627")
    def "Cleans up outdated component configuration"() {
        setup:
        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: true)
        registerService(WorkspaceOperations, operations)

        File root = dir("wtp-project") {
            dir 'src/main/java'
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse-wtp'
            """
            dir('.settings') {
                file 'org.eclipse.wst.common.component', '''<?xml version="1.0" encoding="UTF-8"?>
                    <project-modules id="moduleCoreId" project-version="1.5.0">
                        <wb-module deploy-name="wtp-project">
                            <property name="context-root" value="wtp-project"/>
                            <wb-resource deploy-path="/WEB-INF/classes" source-path="/src/main/java"/>
                            <wb-resource deploy-path="/WEB-INF/classes" source-path="/src/test/java"/>
                        </wb-module>
                    </project-modules>
                '''
            }
        }

        when:
        importAndWait(root)
        def content = new File(root, '.settings/org.eclipse.wst.common.component').text
        def rootNode = new XmlSlurper().parseText(content)
        def moduleConfig = rootNode.'wb-module'.children()

        then:
        moduleConfig.size() == 2
        moduleConfig[0].name() == 'property'
        moduleConfig[1].name() == 'wb-resource'
        moduleConfig[1].'@deploy-path' == '/WEB-INF/classes'
        moduleConfig[1].'@source-path' == 'src/main/java'
    }


    def "wtp projects defined in included builds are ignored"() {
        setup:
        File included
        File root = dir("project") {
            included = dir("included") {
                file "build.gradle", """
                    apply plugin: 'war'
                    apply plugin: 'eclipse'
                 """
                file "settings.gradle", ""
            }
            file 'settings.gradle', "includeBuild 'included'"
        }

        WorkspaceOperations operations = new WorkspaceOperationsDelegate(recognizeWtpComponentNature: true)
        registerService(WorkspaceOperations, operations)

        when:
        importAndWait(root)

        then:
        findProject('project')
        findProject('included')
        !hasComponentDescriptor(included)
        !hasFacetDescriptor(included)
    }

    class WorkspaceOperationsDelegate {
        @Delegate WorkspaceOperations delegate = new DefaultWorkspaceOperations()
        boolean recognizeWtpComponentNature

        boolean isNatureRecognizedByEclipse(String nature) {
            // hacky way to ensure the ProjectNatureUpdater doesn't actually set the WTP nature
            // as it is not part of the test target platform and makes the synchronization fail
            def natureUpdaterCalled = new Exception().stackTrace.find { StackTraceElement element -> element.className == ProjectNatureUpdater.class.name && element.methodName == 'toNatures' }
            nature == WTP_COMPONENT_NATURE && !natureUpdaterCalled ? recognizeWtpComponentNature : delegate.isNatureRecognizedByEclipse(nature)
        }
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
