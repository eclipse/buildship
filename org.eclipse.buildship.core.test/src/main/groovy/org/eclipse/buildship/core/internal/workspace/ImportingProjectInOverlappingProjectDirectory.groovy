/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.eclipse.core.resources.IMarker

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingProjectInOverlappingProjectDirectory extends ProjectSynchronizationSpecification {

    def "Importing projects with overlapping project directory gives warning"() {
        setup:
        Logger logger = Mock(Logger)
        registerService(Logger, logger)
        File rootProject = fileTree(dir('overlapping-project-dir')) {
            file 'settings.gradle', """
                include('sub1')
                include('sub2')

                rootProject.children.find { it.name == 'sub1' }.projectDir = file('sub')
                rootProject.children.find { it.name == 'sub2' }.projectDir = file('sub')
            """
            dir "sub"
        }

        when:
        SynchronizationResult result = tryImportAndWait(rootProject)

        then:
        result.status.isOK()
        getGradleErrorMarkers(findProject('sub1')).size() == 1
        getGradleErrorMarkers(findProject('sub1'))[0].getAttribute(IMarker.MESSAGE) == "The Gradle build declares more than one sub-projects at this location"
    }
}
