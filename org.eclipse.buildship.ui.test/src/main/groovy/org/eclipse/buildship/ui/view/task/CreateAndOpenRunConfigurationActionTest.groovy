package org.eclipse.buildship.ui.view.task

import com.google.common.base.Optional
import org.eclipse.buildship.core.launch.GradleLaunchConfigurationManager
import org.eclipse.buildship.ui.UiPluginConstants
import org.eclipse.buildship.ui.generic.NodeSelection
import org.eclipse.buildship.ui.test.fixtures.TestEnvironment
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jface.viewers.StructuredSelection

class CreateAndOpenRunConfigurationActionTest extends ViewSpecification {

    CreateRunConfigurationAction createAction
    OpenRunConfigurationAction openAction

    def setup() {
        createAction = new CreateRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID)
        openAction = new OpenRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID)
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    def "No action is visible when the nothing is selected"() {
        setup:
        def selection = NodeSelection.empty()

        expect:
        !createAction.isVisibleFor(selection)
        !createAction.isEnabledFor(selection)
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)
    }

    def "Only one action is visible and enabled when a task node is selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def taskNode = newProjectTaskNode(rootNode, ':a')
        def selection = NodeSelection.from(new StructuredSelection(taskNode))

        expect:
        createAction.isVisibleFor(selection) != runConfigurationAlreadyExists
        createAction.isEnabledFor(selection) != runConfigurationAlreadyExists
        openAction.isVisibleFor(selection) == runConfigurationAlreadyExists
        openAction.isEnabledFor(selection) == runConfigurationAlreadyExists

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Only one action is visible and enabled when multiple task nodes are selected from the same project"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def taskNode1 = newProjectTaskNode(rootNode, ':a')
        def taskNode2 = newProjectTaskNode(rootNode, ':b')
        def selection = NodeSelection.from(new StructuredSelection([taskNode1, taskNode2]))

        expect:
        createAction.isVisibleFor(selection) != runConfigurationAlreadyExists
        createAction.isEnabledFor(selection) != runConfigurationAlreadyExists
        openAction.isVisibleFor(selection) == runConfigurationAlreadyExists
        openAction.isEnabledFor(selection) == runConfigurationAlreadyExists

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Only one action is visible and enabled when a project node is selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def selection = NodeSelection.from(new StructuredSelection(rootNode))

        expect:
        createAction.isVisibleFor(selection) != runConfigurationAlreadyExists
        createAction.isEnabledFor(selection) != runConfigurationAlreadyExists
        openAction.isVisibleFor(selection) == runConfigurationAlreadyExists
        openAction.isEnabledFor(selection) == runConfigurationAlreadyExists

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Create action is visible but disabled when multiple projects nodes are selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def projectNode1 = newProjectNode(rootNode, '/root/a')
        def projectNode2 = newProjectNode(rootNode, '/root/b')
        def selection = NodeSelection.from(new StructuredSelection([projectNode1, projectNode2]))

        expect:
        createAction.isVisibleFor(selection)
        !createAction.isEnabledFor(selection)
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Create action is visible but disabled when task nodes from multiple projects are selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def projectNode1 = newProjectNode(rootNode, '/root/a')
        def projectNode2 = newProjectNode(rootNode, '/root/b')
        def taskNode1 = newProjectTaskNode(projectNode1, ':a:one')
        def taskNode2 = newProjectTaskNode(projectNode2, ':b:two')
        def selection = NodeSelection.from(new StructuredSelection([taskNode1, taskNode2]))

        expect:
        createAction.isVisibleFor(selection)
        !createAction.isEnabledFor(selection)
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "No action is visible nor enabled when a project and tasks from multiple projects are selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.registerService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))

        def rootNode = newProjectNode(null, '/root')
        def taskNode = newProjectTaskNode(rootNode, ':a:one')
        def selection = NodeSelection.from(new StructuredSelection([rootNode, taskNode]))

        expect:
        !createAction.isVisibleFor(selection)
        !createAction.isEnabledFor(selection)
        !openAction.isVisibleFor(selection)
        !openAction.isEnabledFor(selection)

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def newTestLaunchConfigurationManager(boolean runConfigurationAlwaysExist) {
        GradleLaunchConfigurationManager manager = Mock(GradleLaunchConfigurationManager)
        if (runConfigurationAlwaysExist) {
            manager.getRunConfiguration(_) >> Optional.of(Mock(ILaunchConfiguration))
        } else {
            manager.getRunConfiguration(_) >> Optional.absent()
        }
        manager
    }

}
