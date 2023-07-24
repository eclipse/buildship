/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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
import spock.lang.Issue

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IStatus
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingWtpProjects extends ProjectSynchronizationSpecification {

    private static final String NON_DEPLOYED = "org.eclipse.jst.component.nondependency"
    private static final String DEPLOYED = "org.eclipse.jst.component.dependency"
    private static final String WTP_COMPONENT_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature"

    def "Do not check mixed deployment paths without WTP installed"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'ear'
                ${jcenterRepositoryBlock}
                dependencies {
                    earlib 'org.assertj:assertj-core:3.10.0'
                    deploy "log4j:log4j:1.2.17"
                }
            """
        }
        Logger logger = Mock(Logger)
        registerService(Logger, logger)
        wtpinstalled = false

        when:
        SynchronizationResult result = tryImportAndWait(root)

        then:
        result.status.severity == IStatus.OK
    }

    def "Check mixed deployment paths with WTP installed"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'ear'
                ${jcenterRepositoryBlock}
                dependencies {
                    earlib 'org.assertj:assertj-core:3.10.0'
                    deploy "log4j:log4j:1.2.17"
                }
            """
        }
        wtpinstalled = true

        when:
        SynchronizationResult result = tryImportAndWait(root)
        IProject project = findProject('project')

        then:
        result.status.severity == IStatus.ERROR
        gradleErrorMarkers.size() == 1
        gradleErrorMarkers[0].getAttribute(IMarker.MESSAGE) == "WTP currently does not support mixed deployment paths."
        gradleErrorMarkers[0].getResource() == project.getFile('build.gradle')
    }

    def "The eclipseWtp task is run before importing WTP projects"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse'
            """
        }
        wtpinstalled = true

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
        wtpinstalled = false

        when:
        importAndWait(root)

        then:
        !hasComponentDescriptor(root)
        !hasFacetDescriptor(root)
    }

    @IgnoreIf({ JavaVersion.current().isJava9Compatible() })
    def "The eclipseWtp task is not run if for Gradle < 3.0"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                apply plugin: 'eclipse'
            """
            file 'settings.gradle', ''
        }
        wtpinstalled = true

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

        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    compile "junit:junit:4.12"
                }
            """
            file 'settings.gradle', ''
        }
        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    implementation "junit:junit:4.12"
                }
            """
        }
        wtpinstalled = true

        when:
        importAndWait(root)

        then:
        def project = findProject('project')
        IClasspathEntry dependency = resolvedClasspath(project).find { it.path.lastSegment() == 'junit-4.12.jar' }
        List attributes = dependency.extraAttributes.findAll { [DEPLOYED, NON_DEPLOYED].contains(it.name) }
        attributes.size() == 0
    }

    def "Deployed attribute is set for deployed dependencies"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                apply plugin: 'war'
                ${jcenterRepositoryBlock}
                dependencies {
                    implementation "junit:junit:4.12"
                }
            """
        }
        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    implementation "junit:junit:4.12"
                }
            """
        }
        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    providedCompile "junit:junit:4.12"
                }
            """
        }
        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    providedCompile "junit:junit:4.12"
                }
            """
        }
        wtpinstalled = true

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
                ${jcenterRepositoryBlock}
                dependencies {
                    deploy "junit:junit:4.12"
                    earlib "com.google.guava:guava:19.0"
                }
                eclipse.classpath.plusConfigurations += [configurations.deploy, configurations.earlib]
            """
        }
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)
        wtpinstalled = true

        when:
        SynchronizationResult result = tryImportAndWait(root)
        IProject project = findProject('project')

        then:
        result.status.severity == IStatus.ERROR
        gradleErrorMarkers.size() == 1
        gradleErrorMarkers[0].getAttribute(IMarker.MESSAGE) == "WTP currently does not support mixed deployment paths."
        gradleErrorMarkers[0].getResource() == project.getFile('build.gradle')
    }

    def "Does not override classpath container customisation"() {
        setup:
        File root = dir("project") {
            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.AccessRule

                apply plugin: 'eclipse'
                apply plugin: 'war'

                ${jcenterRepositoryBlock}

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
        wtpinstalled = true

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
        wtpinstalled = true

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
        wtpinstalled = true

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
        boolean wtpInstalled

        boolean isWtpInstalled () {
            wtpInstalled
        }

        boolean isNatureRecognizedByEclipse(String nature) {
            // hacky way to ensure the ProjectNatureUpdater doesn't actually set the WTP nature
            // as it is not part of the test target platform and makes the synchronization fail
            def natureUpdaterCalled = new Exception().stackTrace.find { StackTraceElement element -> element.className == ProjectNatureUpdater.class.name && element.methodName == 'toNatures' }
            nature == WTP_COMPONENT_NATURE && !natureUpdaterCalled ? wtpInstalled : delegate.isNatureRecognizedByEclipse(nature)
        }
    }

    private void setWtpinstalled(boolean installed) {
        WorkspaceOperations operations = new WorkspaceOperationsDelegate(wtpInstalled: installed)
        registerService(WorkspaceOperations, operations)
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
