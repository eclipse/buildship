package org.eclipse.buildship.ui.view.task

import com.google.common.base.Optional
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path
import spock.lang.Specification

/**
 * Base class for tests related to Gradle Views.
 */
abstract class ViewSpecification extends Specification {

  protected def newProjectNode(ProjectNode parent, String projectLocation) {
    return new ProjectNode(parent, newEclipseProject(projectLocation), newGradleProject(), Optional.absent())
  }

  protected ProjectTaskNode newProjectTaskNode(ProjectNode parent, String taskPath) {
    OmniProjectTask projectTask = Mock(OmniProjectTask)
    projectTask.getPath() >> Path.from(taskPath)
    new ProjectTaskNode(parent, projectTask)
  }

  protected TaskSelectorNode newTaskSelectorNode(ProjectNode parent) {
    OmniTaskSelector taskSelector = Mock(OmniTaskSelector)
    new TaskSelectorNode(parent, taskSelector)
  }

  private OmniEclipseProject newEclipseProject(String path) {
    OmniEclipseProject project = Mock(OmniEclipseProject)
    project.getProjectDirectory() >> new File(path)
    project
  }

  private OmniGradleProject newGradleProject() {
    Mock(OmniGradleProject)
  }

}
