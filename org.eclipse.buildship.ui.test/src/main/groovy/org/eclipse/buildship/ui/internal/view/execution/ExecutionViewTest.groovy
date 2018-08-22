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
        synchronizeAndWait(projectDir)
        launchTaskAndWait(projectDir, 'foo')

        then:
        !currentTree.allItems[0].cell(1).contains('Running')
    }
}
