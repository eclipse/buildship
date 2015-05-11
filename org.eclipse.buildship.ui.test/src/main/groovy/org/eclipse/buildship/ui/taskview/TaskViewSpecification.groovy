package org.eclipse.buildship.ui.taskview

import com.google.common.base.Optional
import com.google.common.collect.ImmutableSortedSet
import com.google.common.collect.AbstractMultiset.ElementSet

import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.OmniGradleProject
import com.gradleware.tooling.toolingmodel.OmniProjectTask
import com.gradleware.tooling.toolingmodel.OmniTaskSelector
import com.gradleware.tooling.toolingmodel.Path

import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.ui.views.tasklist.NewTaskAction

import org.eclipse.buildship.core.launch.GradleLaunchConfigurationManager

import spock.lang.Specification

abstract class TaskViewSpecification extends Specification {

    // this should only be an utility class, but class extend is used because the Mock objects are
    // only available this way

    def newTestLaunchConfigurationManager(boolean runConfigurationAlwaysExist) {
        GradleLaunchConfigurationManager manager = Mock(GradleLaunchConfigurationManager)
        if (runConfigurationAlwaysExist) {
            manager.getRunConfiguration(_) >> Optional.of(Mock(ILaunchConfiguration))
        } else {
            manager.getRunConfiguration(_) >> Optional.absent()
        }
        manager
    }

    // root     ->  build.xml= task a << {}
    //  |                      task b << {}
    //  |-- sub ->  build.xml= task a << {}
    def getFakeNodesV1() {
        def result = [:]
        def root = newProjectNode(null, '/root')
        result['root'] = root
        def ras = newTaskSelectorkNode(root, ':a', ':sub:a')
        result['root_a_taskselector'] = ras
        def rat = newProjectTaskNode(root, ':a')
        result['root_a_projecttask'] = rat
        def rbs = newTaskSelectorkNode(root, ':b')
        result['root_b_taskselector'] = rbs
        def rbt = newProjectTaskNode(root, ':b')
        result['root_b_projecttask'] = rbt
        def sub = newProjectNode(root, '/root/sub')
        result['sub'] = sub
        def sas = newTaskSelectorkNode(sub, 'sub:a')
        result['sub_a_taskselector'] = sas
        def sat = newProjectTaskNode(sub, ':sub:a')
        result['sub_a_projecttask'] = sat

        result
    }

    private def newProjectNode(ProjectNode parent, String location) {
        return new ProjectNode(parent, newEclipseProject(location), Mock(OmniGradleProject), Optional.absent())
    }

    private def newProjectTaskNode(ProjectNode parent, String path) {
        OmniProjectTask projectTask = Mock(OmniProjectTask)
        projectTask.getPath() >> Path.from(path)
        new ProjectTaskNode(parent, projectTask)
    }

    private def newTaskSelectorkNode(ProjectNode parent, String... paths) {
        OmniTaskSelector taskSelector = Mock(OmniTaskSelector)
        taskSelector.getSelectedTaskPaths() >> {
            def result = []
            paths.each { result += Path.from(it) }
            ImmutableSortedSet.copyOf(result)
        }
        new TaskSelectorNode(parent, taskSelector)
    }

    private def newEclipseProject(String path) {
        OmniEclipseProject project = Mock(OmniEclipseProject)
        project.getProjectDirectory() >> new File(path)
        project
    }
}
