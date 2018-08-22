package org.eclipse.buildship.core.internal.workspace.impl

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.configuration.WorkspaceConfiguration
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizingBuildScriptUpdateListenerTest extends ProjectSynchronizationSpecification {

    WorkspaceConfiguration workspaceConfig

    def setup() {
        workspaceConfig = configurationManager.loadWorkspaceConfiguration()
    }

    def cleanup() {
        configurationManager.saveWorkspaceConfiguration(workspaceConfig)
    }

    def "Execute project synchronization when build.gradle file created"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableProjectAutoSync(project)

        when:
        String buildScript = """
            apply plugin: "java"
            ${jcenterRepositoryBlock}
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        """
        project.getFile('build.gradle').create(new ByteArrayInputStream(buildScript.bytes), false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        waitFor { JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') } }
    }

    def "Execute project synchronization when build.gradle file changes"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', """
                allprojects {
                    ${jcenterRepositoryBlock}
                    apply plugin: 'java'
                }
            """
        }

        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableProjectAutoSync(project)

        when:
        String buildScript = """
            apply plugin: "java"
            ${jcenterRepositoryBlock}
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        """

        project.getFile('build.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        waitFor { JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') } }
    }

    def "Execute project synchronization when build.gradle file deleted"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', 'apply plugin: "java"'
        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableProjectAutoSync(project)

        expect:
        JavaCore.create(project).exists()

        when:
        project.getFile('build.gradle').delete(true, false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        !JavaCore.create(project).exists()
    }

    def "Execute project synchronization when custom build script changes"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'custom.gradle', """
                allprojects {
                    apply plugin: 'java'
                    ${jcenterRepositoryBlock}
                }
            """
            file 'settings.gradle', "rootProject.buildFileName = 'custom.gradle'"

        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableProjectAutoSync(project)

        when:
        String buildScript = """
            apply plugin: "java"
            ${jcenterRepositoryBlock}
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        """
        project.getFile('custom.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        waitFor { JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') } }
    }

    def "Synchronization can be disabled for the entire workspace"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
        }
        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        disableWorkspaceAutoSync()
        inheritWorkspacePreferences(project)

        when:
        String buildScript = 'apply plugin: "java"'
        project.getFile('build.gradle').create(new ByteArrayInputStream(buildScript.bytes), false, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        !JavaCore.create(project).exists()
    }

    def "Synchronization can be disabled for a project"() {
        setup:
        File projectDir = dir('auto-sync-test-project') {
            dir('src/main/java')
            file 'build.gradle', """
                allprojects {
                    apply plugin: 'java'
                    ${jcenterRepositoryBlock}
                }
            """
        }

        importAndWait(projectDir)
        IProject project = findProject('auto-sync-test-project')
        enableWorkspaceAutoSync()
        disableProjectAutoSync(project)

        when:
        String buildScript = """
            apply plugin: "java"
            ${jcenterRepositoryBlock}
            dependencies { compile "org.springframework:spring-beans:1.2.8" }
        """
        project.getFile('build.gradle').setContents(new ByteArrayInputStream(buildScript.bytes), 0, new NullProgressMonitor())
        waitForResourceChangeEvents()
        waitForGradleJobsToFinish()

        then:
        waitFor { !JavaCore.create(project).getResolvedClasspath(false).find { it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') } }
    }

    private void disableWorkspaceAutoSync() {
        setWorkspaceAutoSync(false)
    }

    private void enableWorkspaceAutoSync() {
        setWorkspaceAutoSync(true)
    }

    private void setWorkspaceAutoSync(boolean autoSync) {
        WorkspaceConfiguration workspaceConfig = new WorkspaceConfiguration(workspaceConfig.gradleDistribution,
            workspaceConfig.gradleUserHome,
            workspaceConfig.gradleIsOffline,
            workspaceConfig.buildScansEnabled,
            autoSync)
        configurationManager.saveWorkspaceConfiguration(workspaceConfig)
    }

    private void disableProjectAutoSync(IProject project) {
        setProjectAutoSync(project, false)
    }

    private void enableProjectAutoSync(IProject project) {
        setProjectAutoSync(project, true)
    }

    private void setProjectAutoSync(IProject project, boolean autoSync) {
        BuildConfiguration currentConfig = configurationManager.loadProjectConfiguration(project).buildConfiguration
        BuildConfiguration updatedConfig = configurationManager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
            true,
            currentConfig.gradleDistribution,
            currentConfig.gradleUserHome,
            currentConfig.buildScansEnabled,
            currentConfig.offlineMode,
            autoSync)
        configurationManager.saveBuildConfiguration(updatedConfig)
    }

    private void inheritWorkspacePreferences(IProject project) {
        BuildConfiguration currentConfig = configurationManager.loadProjectConfiguration(project).buildConfiguration
        BuildConfiguration updatedConfig = configurationManager.createBuildConfiguration(currentConfig.getRootProjectDirectory(),
            false,
            currentConfig.gradleDistribution,
            currentConfig.gradleUserHome,
            currentConfig.buildScansEnabled,
            currentConfig.offlineMode,
            true)
        configurationManager.saveBuildConfiguration(updatedConfig)
    }
}
