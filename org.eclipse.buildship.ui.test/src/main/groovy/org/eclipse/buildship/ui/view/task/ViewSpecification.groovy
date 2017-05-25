package org.eclipse.buildship.ui.view.task

import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.BuildConfiguration
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
    BuildConfiguration buildConfiguration = CorePlugin.configurationManager().createBuildConfiguration(projectDir, GradleDistribution.fromBuild(), null, false, false, false)
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
