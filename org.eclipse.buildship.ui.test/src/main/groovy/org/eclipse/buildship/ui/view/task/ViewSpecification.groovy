package org.eclipse.buildship.ui.view.task

import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path

/**
 * Base class for tests related to Gradle Views.
 */
abstract class ViewSpecification extends Specification {

  protected def newProjectNode(ProjectNode parent, String projectLocation) {
    return new ProjectNode(parent, newEclipseProject(parent, projectLocation), newGradleProject(), Optional.absent())
  }

  protected ProjectTaskNode newProjectTaskNode(ProjectNode parent, String taskPath) {
    OmniProjectTask projectTask = Stub(OmniProjectTask) {
        getPath() >> Path.from(taskPath)
    }
    new ProjectTaskNode(parent, projectTask)
  }

  protected TaskSelectorNode newTaskSelectorNode(ProjectNode parent) {
    OmniTaskSelector taskSelector = Stub(OmniTaskSelector)
    new TaskSelectorNode(parent, taskSelector)
  }

  private OmniEclipseProject newEclipseProject(ProjectNode parentNode, String path) {
    OmniEclipseProject eclipseProject = Stub(OmniEclipseProject) {
        getProjectDirectory() >> new File(path)
        getParent() >> parentNode?.eclipseProject
        getRoot() >> (parentNode?.eclipseProject?.root ?: it)
    }
  }

  private OmniGradleProject newGradleProject() {
    Stub(OmniGradleProject)
  }

}
