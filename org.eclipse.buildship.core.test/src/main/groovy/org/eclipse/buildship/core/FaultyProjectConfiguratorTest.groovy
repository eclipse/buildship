/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import org.eclipse.core.resources.IMarker
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus

class FaultyProjectConfiguratorTest extends BaseProjectConfiguratorTest {

    def "Synchronization finishes even if contributed configurator fails to initialize"() {
        setup:
        File location = dir("FaultyProjectConfiguratorTest_1") {
            file 'settings.gradle', ''
        }

        registerConfigurator(new FaultyInit())

        when:
        SynchronizationResult result = tryImportAndWait(location)
        def loggedErrors = platformLogErrors.findAll { it.plugin == 'pluginId' }

        then:
        result.status.severity == IStatus.ERROR
        loggedErrors.size() == 1
        loggedErrors[0].message == "Project configurator 'configurator1' failed to initialize"
        gradleErrorMarkers.size() == 1
        gradleErrorMarkers[0].getAttribute(IMarker.MESSAGE) == "Project configurator 'configurator1' failed to initialize"
    }

    def "Synchronization finishes even if contributed configurator throws exception in configure()"() {
        setup:
        File location = dir("FaultyProjectConfiguratorTest_2") {
            file 'settings.gradle', ''
        }
        registerConfigurator(new FaultyConfigure())

        when:
        SynchronizationResult result = tryImportAndWait(location)

        then:
        result.status.severity == IStatus.ERROR
        platformLogErrors.size() == 1
        platformLogErrors[0].message == "Project configurator 'configurator1' failed to configure project 'FaultyProjectConfiguratorTest_2'"
        gradleErrorMarkers.size() == 1
        gradleErrorMarkers[0].getAttribute(IMarker.MESSAGE) == "Project configurator 'configurator1' failed to configure project 'FaultyProjectConfiguratorTest_2'"
    }

    def "Synchronization finishes even if contributed configurator throws exception in unconfigure()"() {
        setup:
        File settingsFile = null
        File location = dir('FaultyProjectConfiguratorTest_3') {
            settingsFile = file "settings.gradle", """
                rootProject.name = 'root'
                include 'sub1'
            """
            dir 'sub1'
        }
        importAndWait(location)
        new File(location, 'settings.gradle').text = "rootProject.name = 'root'"
        registerConfigurator(new FaultyUnconfigure())

        when:
        SynchronizationResult result = trySynchronizeAndWait(location)

        then:
        result.status.severity == IStatus.ERROR
        platformLogErrors.size() == 1
        platformLogErrors[0].message == "Project configurator 'configurator1' failed to unconfigure project 'sub1'"
        gradleErrorMarkers.size() == 1
        gradleErrorMarkers[0].getAttribute(IMarker.MESSAGE) == "Project configurator 'configurator1' failed to unconfigure project 'sub1'"
    }

    static class NoOp implements ProjectConfigurator {
        void init(InitializationContext context, IProgressMonitor monitor) { }
        void configure(ProjectContext context, IProgressMonitor monitor) { }
        void unconfigure(ProjectContext context, IProgressMonitor monitor) { }
    }

    static class FaultyInit extends NoOp {
        void init(InitializationContext context, IProgressMonitor monitor) { throw new Exception() }
    }

    static class FaultyConfigure extends NoOp {
        void configure(ProjectContext context, IProgressMonitor monitor) { throw new Exception() }
    }

    static class FaultyUnconfigure extends NoOp {
        void unconfigure(ProjectContext context, IProgressMonitor monitor) { throw new Exception() }
    }
}
