package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.event.Event
import org.eclipse.buildship.core.event.EventListener
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent
import org.eclipse.buildship.core.workspace.ProjectDeletedEvent
import org.eclipse.buildship.core.workspace.ProjectMovedEvent

class ProjectChangeListenerTest extends WorkspaceSpecification {

    EventListener listener

    void setup() {
        newProject('existing-project')
        listener = Mock(EventListener)
        CorePlugin.listenerRegistry().addEventListener(listener)
    }

    void cleanup() {
        CorePlugin.listenerRegistry().removeEventListener(listener)
    }

    def "Can listen to project creation events"() {
        when:
        newProject('project')

        then:
        1 * listener.onEvent(_) >> { Event e ->
            assert e instanceof ProjectCreatedEvent
            assert e.project.name == 'project'
        }
    }

    def "Can listen to project deletion events"() {
        when:
        deleteAllProjects(true)

        then:
        1 * listener.(_) >> { Event e ->
            assert e instanceof ProjectDeletedEvent
            assert e.project.name == 'existing-project'
        }
    }

    def "Can listen to project rename events"() {
        when:
        CorePlugin.workspaceOperations().renameProject(findProject('existing-project'), 'moved-project', new NullProgressMonitor())

        then:
        1 * listener.onEvent(_) >> { Event e ->
            assert e instanceof ProjectMovedEvent
            assert e.project.name == 'moved-project'
            assert e.previousName == 'existing-project'
        }
    }
}
