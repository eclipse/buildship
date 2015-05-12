package org.eclipse.buildship.ui.taskview

import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.ui.UiPluginConstants
import org.eclipse.buildship.ui.generic.NodeSelection
import org.eclipse.buildship.ui.test.fixtures.TestEnvironment


class OpenBuildScriptActionTest extends TaskViewSpecification {
    OpenBuildScriptAction openAction = new OpenBuildScriptAction(UiPluginConstants.OPEN_BUILD_SCRIPT_COMMAND_ID)
    def nodes

    def setup() {
        nodes = fakeNodesV1
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    def "Nothing is selected"() {
        setup:
        def selection = NodeSelection.empty()

        expect:
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)
    }

    def "Action visible and enabled when a single project node is selected"() {
        setup:
        def selection = NodeSelection.from(new StructuredSelection(nodes['root']))

        expect:
        openAction.isVisibleFor(selection)
        openAction.isEnabledFor(selection)
    }

    def "Action visible but not enabled when multiple project is selected"() {
        setup:
        def selection = NodeSelection.from(new StructuredSelection(nodes['root'], nodes['sub']))

        expect:
        openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)
    }

    def "Action is not visible nor enabled when a task node is seleted"() {
        setup:
        def selection = NodeSelection.from(new StructuredSelection(nodes['root_a_projecttask']))

        expect:
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)
    }

    def "Action is not visible nor enabled when a project and a task node is seleted"() {
        setup:
        def selection = NodeSelection.from(new StructuredSelection(nodes['root'], nodes['root_a_projecttask']))

        expect:
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)
    }

}
