/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch

import static org.gradle.api.JavaVersion.VERSION_13

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf
import spock.lang.Requires

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.eclipse.PlatformUtils

class GradleClasspathProviderUpdateTest extends ProjectSynchronizationSpecification {

    ILaunchConfiguration launchConfiguration

    def setup() {
        ILaunchConfigurationWorkingCopy launchConfigWorkingCopy = createLaunchConfig(
            SupportedLaunchConfigType.JDT_JAVA_APPLICATION.id,
            'launch config for' + GradleClasspathProviderUpdateTest.class.simpleName)
        launchConfigWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 'project-name')
        launchConfiguration = launchConfigWorkingCopy.doSave()
    }

    @Requires({ PlatformUtils.supportsTestAttributes() })
    def "Classpath provider not used if Eclipse version supporting test attributes"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }

        when:
        importAndWait(projectDir)

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Classpath provider added when referenced project is a new Java project"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }

        when:
        importAndWait(projectDir, GradleDistribution.forVersion("5.5"))

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    def "Classpath provider not added for new non-Java project"() {
        setup:
        File projectDir = dir('project-name') {
            file 'settings.gradle', ''
        }

        when:
        importAndWait(projectDir)

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    @IgnoreIf({ PlatformUtils.supportsTestAttributes() })
    def "Gradle classpath provider injected when project is moved under target name"() {
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

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Classpath provider injected when project using old Gradle version is moved under target name"() {
        setup:
        File settingsFile
        File projectDir = dir('root-project') {
             settingsFile = file 'settings.gradle', 'include "old-name"'
             file 'build.gradle', 'allprojects { apply plugin: "java" }'
             dir('old-name')
             dir('project-name')
        }
        importAndWait(projectDir, GradleDistribution.forVersion("5.5"))

        expect:
        findProject('old-name')
        !hasGradleClasspathProvider(launchConfiguration)


        when:
        settingsFile.text = 'include "project-name"'
        synchronizeAndWait(projectDir)

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }


    @IgnoreIf({ PlatformUtils.supportsTestAttributes() })
    def "Classpath provider removed when project deleted"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir)

        expect:
        hasGradleClasspathProvider(launchConfiguration)

        when:
        findProject('project-name').delete(false, new NullProgressMonitor())

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Classpath provider removed when project using old Gradle version deleted"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir,  GradleDistribution.forVersion("5.5"))

        expect:
        hasGradleClasspathProvider(launchConfiguration)

        when:
        findProject('project-name').delete(false, new NullProgressMonitor())

        then:
        !hasGradleClasspathProvider(launchConfiguration)
    }

    def "Classpath provider added when Gradle nature added"() {
        setup:
        IJavaProject javaProject = newJavaProject('project-name')

        expect:
        !hasGradleClasspathProvider(launchConfiguration)

        when:
        CorePlugin.workspaceOperations().addNature(javaProject.project, GradleProjectNature.ID, new NullProgressMonitor())

        then:
        hasGradleClasspathProvider(launchConfiguration)
    }

    @IgnoreIf({ PlatformUtils.supportsTestAttributes() })
    def "Classpath provider removed when Gradle nature removed"() {
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

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Classpath provider removed when Gradle nature removed from project using Old Gradle version"() {
        setup:
        File projectDir = dir('project-name') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir, GradleDistribution.forVersion("5.5"))

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
        configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String) null)
    }
}
