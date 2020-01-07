/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import spock.lang.Issue

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.test.fixtures.TestEnvironment.*

class ImportingMultiComposite extends ProjectSynchronizationSpecification {

    @Issue('https://github.com/eclipse/buildship/issues/908')
    def "Can import composite build that includes the same build multiple times"() {
        setup:
        File projectDir
        fileTree(dir('multi-composite')) {
                projectDir = dir('A') {
                file 'settings.gradle', """
                    includeBuild('../B')
                    includeBuild('../C')
                """
            }
            dir('B') {
                file 'settings.gradle', """
                    includeBuild('../C')
                """
            }
            dir('C')
        }

        when:
        SynchronizationResult result = tryImportAndWait(projectDir)

        then:
        result.status.isOK()
    }
}
