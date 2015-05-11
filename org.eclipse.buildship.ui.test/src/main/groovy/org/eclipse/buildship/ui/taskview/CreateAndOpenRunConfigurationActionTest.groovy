package org.eclipse.buildship.ui.taskview

import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.Path
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.OmniEclipseProject

import org.eclipse.core.resources.IProject
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jface.viewers.StructuredSelection

import org.eclipse.buildship.core.launch.GradleLaunchConfigurationManager
import org.eclipse.buildship.ui.UiPluginConstants
import org.eclipse.buildship.ui.generic.NodeSelection
import org.eclipse.buildship.ui.test.fixtures.TestEnvironment


class CreateAndOpenRunConfigurationActionTest extends TaskViewSpecification {

    CreateRunConfigurationAction createAction
    OpenRunConfigurationAction openAction
    def nodes

    def setup() {
        createAction = new CreateRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID)
        openAction = new OpenRunConfigurationAction(UiPluginConstants.OPEN_RUN_CONFIGURATION_COMMAND_ID)
        nodes = fakeNodesV1
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
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root_a_projecttask']))

        expect:
        createAction.isVisibleFor(selection) != runConfigurationAlreadyExists
        createAction.isEnabledFor(selection) != runConfigurationAlreadyExists
        openAction.isVisibleFor(selection) == runConfigurationAlreadyExists
        openAction.isEnabledFor(selection) == runConfigurationAlreadyExists

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Only one action is visble and enabled when multiple task nodes are selected from the same project"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root_a_projecttask'], nodes['root_b_projecttask']))

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
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root']))

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
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root'], nodes['sub']))

        expect:
        createAction.isVisibleFor(selection) == true
        createAction.isEnabledFor(selection) == false
        openAction.isVisibleFor(selection) == false
        openAction.isEnabledFor(selection) == false

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "Create action is visible but disabled when task nodes from multiple projects are selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root_a_projecttask'], nodes['sub_a_projecttask']))

        expect:
        createAction.isVisibleFor(selection) == true
        createAction.isEnabledFor(selection) == false
        openAction.isVisibleFor(selection) == false
        openAction.isEnabledFor(selection) == false

        where:
        runConfigurationAlreadyExists << [true, false]
    }

    def "No action is visible nor enabled when a project and tasks from multiple projects are selected"(boolean runConfigurationAlreadyExists) {
        setup:
        TestEnvironment.testService(GradleLaunchConfigurationManager, newTestLaunchConfigurationManager(runConfigurationAlreadyExists))
        def selection = NodeSelection.from(new StructuredSelection(nodes['root'], nodes['root_a_projecttask'], nodes['sub_a_projecttask']))

        expect:
        createAction.isVisibleFor(selection) == false
        createAction.isEnabledFor(selection) == false
        openAction.isVisibleFor(selection) == false
        openAction.isEnabledFor(selection) == false

        where:
        runConfigurationAlreadyExists << [true, false]
    }
}
