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

import spock.lang.Ignore

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer

@Ignore
class SynchronizingExistingWorkspaceProject extends SingleProjectSynchronizationSpecification {

    @Ignore('Now we update project settingsfor closed projects too')
    def "If the project is closed, then the project remains untouched"() {
        setup:
        IProject project = newClosedProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }
        File[] projectFiles = dir('sample-project').listFiles()
        Long[] modifiedTimes = projectFiles.collect { it.lastModified() }

        when:
        importAndWait(projectDir)
        def updatedProjectFiles = dir('sample-project').listFiles().findAll { it.name != '.gradle' }

        then:
        !project.isOpen()
        projectFiles == updatedProjectFiles
        modifiedTimes == updatedProjectFiles.collect{ it.lastModified() }
    }

    def "The Gradle classpath container is updated"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        IClasspathEntry[] entries = javaProject.rawClasspath + JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)
        javaProject.setRawClasspath(entries, null)
        def projectDir = dir('sample-project') {
            file 'build.gradle', """apply plugin: "java"
               ${jcenterRepositoryBlock}
               dependencies { implementation "org.springframework:spring-beans:1.2.8"}
            """
            dir 'src/main/java'
        }

        expect:
        !javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        importAndWait(projectDir)

        then:
        javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    @Override
    protected void prepareProject(String name) {
        newProject(name)
    }

    @Override
    protected void prepareJavaProject(String name) {
        newJavaProject(name)
    }
}
