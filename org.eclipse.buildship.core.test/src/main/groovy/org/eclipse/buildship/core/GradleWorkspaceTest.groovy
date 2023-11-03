/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleWorkspaceTest extends ProjectSynchronizationSpecification {

    def "Cannot get a null build"() {
        expect:
        !GradleCore.workspace.getBuild(null).present
    }

    def "Cannot get a Gradle build if target project is inaccessible"() {
        expect:
        !GradleCore.workspace.getBuild(newClosedProject("GradleWorkspaceTest")).present
    }

    def "Cannot get a Gradle build if target project is not a Gradle project"() {
        expect:
        !GradleCore.workspace.getBuild(newProject("GradleWorkspaceTest")).present
    }

    def "Can get a Gradle build via valid Gradle project"() {
        setup:
        File projectDir = dir('GradleWorkspaceTest') {
            file 'settings.gradle', ''
        }
        importAndWait(projectDir)
        IProject project = findProject("GradleWorkspaceTest")

        expect:
        GradleCore.workspace.getBuild(project).present
    }

    def "Cannot create a null build"() {
        when:
        GradleCore.workspace.createBuild(null)

        then:
        thrown(NullPointerException)
    }

    def "Can create a Gradle build"() {
        setup:
        File projectDir = dir('GradleWorkspaceTest') {
            file 'settings.gradle', ''
        }

        BuildConfiguration configuration = BuildConfiguration.forRootProjectDirectory(projectDir).build()

        expect:
        GradleCore.workspace.createBuild(configuration)
    }
}
