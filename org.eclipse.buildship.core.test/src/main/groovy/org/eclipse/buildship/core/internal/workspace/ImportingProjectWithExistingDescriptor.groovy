/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import com.google.common.base.Optional

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.ILogListener
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer

class ImportingProjectWithExistingDescriptor extends SingleProjectSynchronizationSpecification {

    def "The project is added to the workspace"() {
        def project = newProject("sample-project")
        project.delete(false, null)
        setup:
        def projectDir = dir('sample-project') {
            file 'settings.gradle', ''
        }

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        importAndWait(projectDir)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If the Gradle classpath container is missing, it is added"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        deleteAllProjects(false)
        def projectDir = dir('sample-project') {
            file 'build.gradle', "apply plugin: 'java'"
        }

        when:
        importAndWait(projectDir)

        then:
        project.hasNature(JavaCore.NATURE_ID)
        JavaCore.create(project).rawClasspath.any { it.path == GradleClasspathContainer.CONTAINER_PATH }
    }

    def "Can import an existing Buildship project using with generic wizard"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'build.gradle', "apply plugin: 'java'"
        }
        importAndWait(projectDir)
        deleteAllProjects(false)

        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)

        expect:
        !findProject('sample-project')

        when:
        Optional<IProjectDescription> description = CorePlugin.workspaceOperations().findProjectDescriptor(projectDir, new NullProgressMonitor());
        CorePlugin.workspaceOperations().includeProject(description.get(), [], new NullProgressMonitor())
        waitForRefreshToFinish()

        then:
        findProject('sample-project')
        platformLogErrors.empty
        0 * logger.warn(*_)
        0 * logger.error(*_)
    }

    @Override
    protected void prepareProject(String name) {
        def project = newProject(name)
        project.delete(false, true, null)
    }

    @Override
    protected void prepareJavaProject(String name) {
        def project = newJavaProject(name).project
        project.delete(false, true, null)
    }

    private void waitForRefreshToFinish() {
        Job.jobManager.join(LegacyEclipseSpockTestHelper.getAutoRefreshJobFamily(), null)
    }
}
