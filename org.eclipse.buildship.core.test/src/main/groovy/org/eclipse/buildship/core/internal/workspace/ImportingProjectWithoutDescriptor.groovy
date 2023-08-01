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

import org.eclipse.buildship.core.internal.CorePlugin

@Ignore
class ImportingProjectWithoutDescriptor extends SingleProjectSynchronizationSpecification {

    def "The project is created and added to the workspace"() {
        setup:
        def projectDir = dir('sample-project') {
            file 'settings.gradle', ''
        }

        expect:
        CorePlugin.workspaceOperations().allProjects.empty

        when:
        importAndWait(projectDir)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    @Override
    protected void prepareProject(String name) {
    }

    @Override
    protected void prepareJavaProject(String name) {
    }
}
