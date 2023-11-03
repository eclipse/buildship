/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution

import spock.lang.Issue

class ExecutionViewTest extends BaseExecutionViewTest {

    @Issue('https://github.com/eclipse/buildship/issues/586')
    def "Shows complete execution tree"() {
        setup:
        File projectDir = dir('project-without-build-scan') {
            file 'build.gradle', 'task foo { }'
        }

        when:
        importAndWait(projectDir)
        launchTaskAndWait(projectDir, 'foo')

        then:
        !currentTree.allItems[0].cell(1).contains('Running')
    }
}
