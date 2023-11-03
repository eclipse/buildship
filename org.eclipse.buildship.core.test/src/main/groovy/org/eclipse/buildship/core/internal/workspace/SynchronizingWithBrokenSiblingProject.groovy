/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import spock.lang.Issue

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizingWithBrokenSiblingProject extends ProjectSynchronizationSpecification {

    @Issue('https://github.com/eclipse/buildship/issues/528')
    def "Broken project does not have affect on unrelated synchronization"() {
        setup:
        def first = dir('first') {
            file 'settings.gradle', ''
        }
        def second = dir('second') {
            file 'settings.gradle', ''
        }

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
