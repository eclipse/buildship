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

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus.ToolingApiStatusType
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingBrokenProject extends ProjectSynchronizationSpecification {

    File projectDir

    def setup() {
        projectDir = dir('broken-project') {
            file 'build.gradle', ''
            file 'settings.gradle', 'include "sub"'
            dir('sub') {
                file 'build.gradle', 'I_AM_ERROR'
            }
        }
    }

    def "can import the root project of a broken build"() {
        when:
        SynchronizationResult result = tryImportAndWait(projectDir)

        then:
        findProject('broken-project')
        !findProject('sub')
    }

    def "if the root project of a broken build is already part of the workspace then the Gradle nature is assigned to it"() {
        when:
        newProject('broken-project')
        SynchronizationResult result = tryImportAndWait(projectDir)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        GradleProjectNature.isPresentOn(findProject('broken-project'))
    }

    def "can import the root project of a broken build, even if the root project name is already taken in the workspace"() {
        setup:
        File existingProjectLocation = dir('another/broken-project') {
            file 'settings.gradle', ''
        }
        importAndWait(existingProjectLocation)

        when:
        SynchronizationResult result = tryImportAndWait(projectDir)

        then:
        findProject("broken-project_").location.toFile() == projectDir.canonicalFile
    }

    def "importing the root project of a broken build fails if the root dir is the workspace root"() {
        when:
        SynchronizationResult result = tryImportAndWait(getWorkspaceDir())

        then:
        ToolingApiStatusType.IMPORT_ROOT_DIR_FAILED.matches(result.status)
        allProjects().empty
    }
}
