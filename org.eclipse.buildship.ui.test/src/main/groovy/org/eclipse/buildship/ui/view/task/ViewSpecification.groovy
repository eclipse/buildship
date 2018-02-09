package org.eclipse.buildship.ui.view.task

import com.google.common.base.Optional

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject
import org.eclipse.buildship.core.omnimodel.OmniGradleProject
import org.eclipse.buildship.core.omnimodel.OmniProjectTask
import org.eclipse.buildship.core.omnimodel.OmniTaskSelector
import org.eclipse.buildship.core.util.gradle.Path
import org.eclipse.buildship.ui.test.fixtures.WorkspaceSpecification

/**
 * Base class for tests related to Gradle Views.
 */
abstract class ViewSpecification extends WorkspaceSpecification {

  protected def newProjectNode(ProjectNode parent, String projectLocation) {
    return new ProjectNode(parent, newEclipseProject(parent, projectLocation), newGradleProject(), Optional.absent(), false)
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
    File projectDir = dir(path)
    BuildConfiguration buildConfiguration = createInheritingBuildConfiguration(projectDir)
    CorePlugin.configurationManager().saveBuildConfiguration(buildConfiguration)
    OmniEclipseProject eclipseProject = Stub(OmniEclipseProject) {
        getProjectDirectory() >> projectDir
        getParent() >> parentNode?.eclipseProject
        getRoot() >> (parentNode?.eclipseProject?.root ?: it)
    }
  }

  private OmniGradleProject newGradleProject() {
    Stub(OmniGradleProject)
  }

}
