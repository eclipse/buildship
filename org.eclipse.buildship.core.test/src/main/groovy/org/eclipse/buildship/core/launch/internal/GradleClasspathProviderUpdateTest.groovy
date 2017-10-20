package org.eclipse.buildship.core.launch.internal

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

class GradleClasspathProviderUpdateTest extends ProjectSynchronizationSpecification {

    ILaunchConfiguration launchConfiguration

    def setup() {
        ILaunchConfigurationWorkingCopy launchConfigWorkingCopy = createLaunchConfig(
            SupportedLaunchConfigType.JDT_JAVA_APPLICATION.id,
            'launch config for' + GradleClasspathProviderUpdateTest.class.simpleName)
        launchConfigWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 'project-name')
        launchConfiguration = launchConfigWorkingCopy.doSave()
    }

    def "Gradle classpath provider added when referenced project is a new Java project"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }

        when:
        importAndWait(projectDir)

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    def "Gradle classpath provider not not added for new jon-Java project"() {
        setup:
        File projectDir = dir('project-name')

        when:
        importAndWait(projectDir)

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    def "Gradle classpath provider injected when Gradle project is moved under target name"() {
        setup:
        File settingsFile
        File projectDir = dir('root-project') {
             settingsFile = file 'settings.gradle', 'include "old-name"'
             file 'build.gradle', 'allprojects { apply plugin: "java" }'
             dir('old-name')
             dir('project-name')
        }
        importAndWait(projectDir)

        expect:
        findProject('old-name')
        !hasGradleClasspathProvider(launchConfiguration)

        when:
        settingsFile.text = 'include "project-name"'
        synchronizeAndWait(projectDir)
        waitForResourceChangeEvents()

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    def "Gradle classpath provider removed when project deleted"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir)

        expect:
        hasGradleClasspathProvider(launchConfiguration)

        when:
        findProject('project-name').delete(false, new NullProgressMonitor())
        waitForResourceChangeEvents()

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    def "Gradle classpath provider added when Gradle nature added"() {
        setup:
        IJavaProject javaProject = newJavaProject('project-name')

        expect:
        !hasGradleClasspathProvider(launchConfiguration)

        when:
        CorePlugin.workspaceOperations().addNature(javaProject.project, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    def "Gradle classpath provider removed when Gradle nature removed"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir)

        expect:
        hasGradleClasspathProvider(launchConfiguration)

        when:
        CorePlugin.workspaceOperations().removeNature(findProject('project-name'), GradleProjectNature.ID, new NullProgressMonitor())

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    private boolean hasGradleClasspathProvider(ILaunchConfiguration configuration) {
        getClasspathProvider(configuration) == GradleClasspathProvider.ID
    }

    private String getClasspathProvider(ILaunchConfiguration configuration) {
        configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null)
    }
}
