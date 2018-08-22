package org.eclipse.buildship.core.internal.workspace.impl

import spock.lang.Issue

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizingWithBrokenSiblingProject extends ProjectSynchronizationSpecification {

    @Issue('https://github.com/eclipse/buildship/issues/528')
    def "Broken project does not have affect on unrelated synchronization"() {
        setup:
        def first = dir('first')
        def second = dir('second')

        importAndWait(first)
        importAndWait(second)

        new File(second, ".settings/${CorePlugin.PLUGIN_ID}.prefs").text  = ''
        findProject('second').refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())

        when:
        synchronizeAndWait(first)

        then:
        getGradleErrorMarkers(findProject('first')).empty
    }
}
