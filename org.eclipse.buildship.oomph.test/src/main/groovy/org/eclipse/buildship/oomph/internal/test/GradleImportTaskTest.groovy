/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph.internal.test

import spock.lang.IgnoreIf

import static org.eclipse.buildship.oomph.DistributionType.*
import static org.gradle.api.JavaVersion.VERSION_13

import org.gradle.api.JavaVersion
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.build.BuildEnvironment
import spock.lang.Unroll

import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl
import org.eclipse.oomph.internal.setup.SetupPrompter
import org.eclipse.oomph.resources.impl.ResourcesFactoryImpl
import org.eclipse.oomph.setup.SetupTaskContext
import org.eclipse.oomph.setup.Stream
import org.eclipse.oomph.setup.Trigger
import org.eclipse.oomph.setup.internal.core.SetupContext
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer

import org.eclipse.buildship.core.BuildConfiguration
import org.eclipse.buildship.core.GradleCore
import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.oomph.DistributionType
import org.eclipse.buildship.oomph.GradleImportFactory
import org.eclipse.buildship.oomph.GradleImportTask
import org.eclipse.buildship.oomph.internal.test.fixtures.ProjectSynchronizationSpecification

// we're accessing internal oomph classes here
@SuppressWarnings("restriction")
class GradleImportTaskTest extends ProjectSynchronizationSpecification {

    static final String GRADLE_URL = 'https://services.gradle.org/distributions/gradle-5.4-bin.zip'
    static final File LOCAL_GRADLE_DIR = installGradleIfNecessary()

    def SetupTaskContext setupTaskContext
    def GradleImportTask importTask = GradleImportFactory.eINSTANCE.createGradleImportTask()

    def setup() {
        setupTaskContext = buildSetupTaskContext(null)
    }

    def "Imports project into workspace"() {
        setup:
        def location = dir('import-into-workspace') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle','rootProject.name = "ImportIntoWorkspace"'
        }
        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(location.absolutePath))

        when:
        importTask.perform(setupTaskContext)

        then:
        def project = findProject("ImportIntoWorkspace")
        project.hasNature(GradleProjectNature.ID)
    }

    def "Manual trigger causes synchronization"() {
        setup:
        setupTaskContext = buildSetupTaskContext(Trigger.MANUAL)
        def location = dir('manual-sync') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle','rootProject.name = "ManualSync"'
        }
        importAndWait(location)
        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(location.absolutePath))

        expect:
        findProject("ManualSync")

        when:
        changeProjectName(location, "ManualSyncChanged")
        importTask.perform(setupTaskContext)

        then:
        findProject("ManualSyncChanged")
        !findProject("ManualSync")
    }

    def "Startup trigger does not synchronize existing projects"() {
        setup:
        setupTaskContext = buildSetupTaskContext(Trigger.STARTUP)
        def location = dir('manual-sync') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle','rootProject.name = "ManualSync"'
        }
        importAndWait(location)
        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(location.absolutePath))

        expect:
        findProject("ManualSync")

        when:
        changeProjectName(location, "ManualSyncChanged")
        importTask.perform(setupTaskContext)

        then:
        findProject("ManualSync")
        !findProject("ManualSyncChanged")
    }

    def "Startup trigger imports projects that are not already in the workspace"() {
        setup:
        setupTaskContext = buildSetupTaskContext(Trigger.STARTUP)
        def projectA = dir('already-present') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle','rootProject.name = "present"'
        }
        importAndWait(projectA)
        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(projectA.absolutePath))

        def projectB = dir('to-import') {
            file 'build.gradle', '''
                plugins {
                    id 'java-library'
                }
            '''
            dir('src/main/java') {
                file 'A.java', ''
            }
            file 'settings.gradle','rootProject.name = "toImport"'
        }
        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(projectB.absolutePath))

        expect:
        findProject("present")

        when:
        changeProjectName(projectA, "presentChanged")
        importTask.perform(setupTaskContext)

        then:
        findProject("present")
        findProject("toImport")
        !findProject("presentChanged")
    }

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.4.1 can run on Java 12 and below
    @Unroll
    def "new build configuration can override workspace settings (#distributionType)"() {
        setup:
        File projectDir = dir('projectDir') { file 'settings.gradle', '''rootProject.name = 'testProject' ''' }
        File projectGradleUserHome = dir('gradle-user-home').canonicalFile
        File projectJavaHome = new File(System.getProperty("java.home")).canonicalFile
        List<String> projectArguments = ['--info']
        List<String> projectJvmArguments = ['-Dfoo=bar']
        String distributionUrl = GRADLE_URL

        importTask.sourceLocators.add(ResourcesFactoryImpl.eINSTANCE.createSourceLocator(projectDir.absolutePath))
        importTask.overrideWorkspaceSettings = true
        importTask.distributionType = distributionType
        importTask.localInstallationDirectory =LOCAL_GRADLE_DIR.canonicalPath
        importTask.remoteDistributionLocation = distributionUrl
        importTask.specificGradleVersion = "5.4.1"
        importTask.programArguments.addAll(projectArguments)
        importTask.jvmArguments.addAll(projectJvmArguments)
        importTask.gradleUserHome = customGradleHome ? projectGradleUserHome : null
        importTask.javaHome = projectJavaHome
        importTask.offlineMode = offlineMode
        importTask.buildScans = buildScansEnabled
        importTask.automaticProjectSynchronization = autoSync
        importTask.showConsoleView = showConsole
        importTask.showExecutionsView = showExecutions

        when:
        importTask.perform(setupTaskContext)

        then:
        def configuration = configurationManager.loadProjectConfiguration(findProject("testProject")).buildConfiguration
        configuration.gradleDistribution == distribution
        configuration.gradleUserHome == (customGradleHome ? projectGradleUserHome : null)
        configuration.javaHome == projectJavaHome
        configuration.overrideWorkspaceSettings == true
        configuration.buildScansEnabled == buildScansEnabled
        configuration.offlineMode == offlineMode
        configuration.autoSync == autoSync
        configuration.arguments == projectArguments
        configuration.jvmArguments == projectJvmArguments
        configuration.showConsoleView == showConsole
        configuration.showExecutionsView == showExecutions

        where:
        // offlineMode == true doesn't return in resonable time, therefore we currently only test false.
        distribution                                                  | distributionType        | offlineMode  | buildScansEnabled | autoSync | showConsole | showExecutions | customGradleHome
        GradleDistribution.fromBuild()                                | GRADLE_WRAPPER          | false        | false             | true     | false       | true           | false
        GradleDistribution.forRemoteDistribution(new URI(GRADLE_URL)) | REMOTE_DISTRIBUTION     | false        | false             | true     | false       | true           | false
        GradleDistribution.forVersion("5.4.1")                        | SPECIFIC_GRADLE_VERSION | false        | true              | false    | true        | false          | false
        GradleDistribution.forLocalInstallation(LOCAL_GRADLE_DIR)     | LOCAL_INSTALLATION      | false        | true              | false    | true        | false          | true
    }

    private static File installGradleIfNecessary() {
        // instead of downloading the zip and unpacking it we let gradle do the work.
        File temp = File.createTempDir();
        temp.deleteOnExit()
        def configuration = BuildConfiguration.forRootProjectDirectory(temp)
                .gradleDistribution(GradleDistribution.forVersion("5.4.1"))
                .overrideWorkspaceConfiguration(true)
                .build()
        BuildEnvironment buildEnvironment = GradleCore.workspace.createBuild(configuration).withConnection({ProjectConnection con -> con.getModel(BuildEnvironment)}, null)
        new File(buildEnvironment.gradle.gradleUserHome, "wrapper/dists/gradle-5.4.1-bin/e75iq110yv9r9wt1a6619x2xm/gradle-5.4.1")
    }

    private changeProjectName(File project, String name) {
        fileTree(project) {
            file("settings.gradle").text = "rootProject.name = '$name'"
        }
    }

    private buildSetupTaskContext(Trigger trigger) {
        new SetupTaskPerformer(new ExtensibleURIConverterImpl(), new SetupPrompter.Default(true), trigger, SetupContext.create(), (Stream)null)
    }
}
