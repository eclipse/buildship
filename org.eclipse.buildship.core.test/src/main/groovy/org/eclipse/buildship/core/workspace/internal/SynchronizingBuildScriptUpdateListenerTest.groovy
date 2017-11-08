package org.eclipse.buildship.core.workspace.internal

import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification

import java.io.File

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

class SynchronizingBuildScriptUpdateListenerTest extends ProjectSynchronizationSpecification {

    WorkspaceConfiguration workspaceConfig

    def setup() {
        workspaceConfig = configurationManager.loadWorkspaceConfiguration()
    }

    def cleanup() {
        configurationManager.saveWorkspaceConfiguration(workspaceConfig)
    }

    private void disableAutoSyncForWorkspace() {
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(workspaceConfig.gradleDistribution, workspaceConfig.gradleUserHome, workspaceConfig.gradleIsOffline, workspaceConfig.buildScansEnabled, false ))
    }

    private void enableAutoSyncForWorkspace() {
        configurationManager.saveWorkspaceConfiguration(new WorkspaceConfiguration(workspaceConfig.gradleDistribution, workspaceConfig.gradleUserHome, workspaceConfig.gradleIsOffline, workspaceConfig.buildScansEnabled, true))
    }

    private void disableAutoSyncForProject(IProject project) {
        BuildConfiguration currentConfig = configurationManager.loadProjectConfiguration(project).buildConfiguration;
        BuildConfiguration updatedConfig = configurationManager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
            true,
            currentConfig.gradleDistribution,
            currentConfig.gradleUserHome,
            currentConfig.buildScansEnabled,
            currentConfig.offlineMode,
            false)
        configurationManager.saveBuildConfiguration(updatedConfig)
    }

    private void enableAutoSyncForProject(IProject project) {
        BuildConfiguration currentConfig = configurationManager.loadProjectConfiguration(project).buildConfiguration;
        BuildConfiguration updatedConfig = configurationManager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
            true,
            currentConfig.gradleDistribution,
            currentConfig.gradleUserHome,
            currentConfig.buildScansEnabled,
            currentConfig.offlineMode,
            true)
        configurationManager.saveBuildConfiguration(updatedConfig)
    }

    private void enableAutoSyncNonOverrideForProject(IProject project) {
        BuildConfiguration currentConfig = configurationManager.loadProjectConfiguration(project).buildConfiguration;
        BuildConfiguration updatedConfig = configurationManager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
            false,
            currentConfig.gradleDistribution,
            currentConfig.gradleUserHome,
            currentConfig.buildScansEnabled,
            currentConfig.offlineMode,
            true)
        configurationManager.saveBuildConfiguration(updatedConfig)
    }

    def "Execute project synchronization when build.gradle file created"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableAutoSyncForProject(project)

        when:
        String buildScript = '''
            apply plugin: "java"
            repositories { jcenter() }
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        '''

        project.getFile('build.gradle').create(new ByteArrayInputStream(buildScript.bytes), false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }.any()
    }

    def "Execute project synchronization when build.gradle file changes"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', '''
                allprojects {
                    repositories { mavenCentral() }
                    apply plugin: 'java'
                }
            '''
        }

        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableAutoSyncForProject(project)

        when:
        String buildScript = '''
            apply plugin: "java"
            repositories { jcenter() }
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        '''

        project.getFile('build.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }.any()
    }

    def "Execute project synchronization when build.gradle file deleted"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', '''
                apply plugin: "java"
                repositories { jcenter() }
                dependencies { compile "org.springframework:spring-beans:1.2.8" }
            '''
        }
        importAndWait(projectDir)

        when:
        IProject project = findProject('auto-sync-test-project')
        enableAutoSyncForProject(project)
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        JavaCore.create(project).exists()

        when:
        project.getFile('build.gradle').delete(true, false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        !(JavaCore.create(project).exists())
    }

    def "Execute project synchronization when custom build script changes"() {

        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'custom.gradle', '''
                allprojects {
                    repositories { mavenCentral() }
                    apply plugin: 'java'
                }
            '''
            file 'settings.gradle', "rootProject.buildFileName = 'custom.gradle'"

        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableAutoSyncForProject(project)

        when:
        String buildScript = '''
            apply plugin: "java"
            repositories { jcenter() }
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        '''

        project.getFile('custom.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }.any()

    }

    def "Synchronization can be disabled for the entire workspace"() {
        setup:
        disableAutoSyncForWorkspace()
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableAutoSyncNonOverrideForProject(project)

        when:
        String buildScript = '''
            apply plugin: "java"
            repositories { jcenter() }
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        '''

        project.getFile('build.gradle').create(new ByteArrayInputStream(buildScript.bytes), false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        !(JavaCore.create(project).exists())

        cleanup:
        enableAutoSyncForWorkspace()
    }

    def "Synchronization can be disabled for a project"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', '''
                allprojects {
                    repositories { mavenCentral() }
                    apply plugin: 'java'
                }
            '''
        }

        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        disableAutoSyncForProject(project)

        when:
        String buildScript = '''
            apply plugin: "java"
            repositories { jcenter() }
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        '''

        project.getFile('build.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        !(JavaCore.create(project).getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }).any()

    }
}
