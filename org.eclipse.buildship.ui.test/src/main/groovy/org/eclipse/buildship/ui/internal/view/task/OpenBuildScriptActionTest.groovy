package org.eclipse.buildship.ui.internal.view.task

import org.eclipse.buildship.ui.internal.UiPluginConstants
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection
import org.eclipse.jface.viewers.StructuredSelection

class OpenBuildScriptActionTest extends ViewSpecification {

  OpenBuildScriptAction openAction

  def setup() {
    openAction = new OpenBuildScriptAction(UiPluginConstants.OPEN_BUILD_SCRIPT_COMMAND_ID)
  }

  def "Action is not visible nor enabled when nothing is selected"() {
    setup:
    def selection = NodeSelection.empty()

    expect:
    !openAction.isVisibleFor(selection)
    !openAction.isEnabledFor(selection)
  }

  def "Action visible and enabled when a single project node is selected"() {
    setup:
    def projectNode = newProjectNode(null, '/location')
    def selection = NodeSelection.from(new StructuredSelection(projectNode))

    expect:
    openAction.isVisibleFor(selection)
    openAction.isEnabledFor(selection)
  }

  def "Action visible and enabled when multiple projects are selected"() {
    setup:
    def projectNode1 = newProjectNode(null, '/location1')
    def projectNode2 = newProjectNode(null, '/location2')
    def selection = NodeSelection.from(new StructuredSelection([projectNode1, projectNode2]))

    expect:
    openAction.isVisibleFor(selection)
    openAction.isEnabledFor(selection)
  }

  def "Action is not visible nor enabled when a task node is selected"() {
    setup:
    def taskNode = newProjectTaskNode(newProjectNode(null, '/location1'), ':path')
    def selection = NodeSelection.from(new StructuredSelection(taskNode))

    expect:
    !openAction.isVisibleFor(selection)
    !openAction.isEnabledFor(selection)
  }

  def "Action is not visible nor enabled when a project and a task node is selected"() {
    setup:
    def projectNode = newProjectNode(null, '/location1')
    def taskNode = newProjectTaskNode(projectNode, ':path')
    def selection = NodeSelection.from(new StructuredSelection([projectNode, taskNode]))

    expect:
    !openAction.isVisibleFor(selection)
    !openAction.isEnabledFor(selection)
  }

}
