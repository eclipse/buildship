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

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
import org.eclipse.buildship.core.BaseProjectConfiguratorTest
import org.eclipse.buildship.core.InitializationContext
import org.eclipse.buildship.core.ProjectConfigurator
import org.eclipse.buildship.core.ProjectContext
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationPerformanceTest extends BaseProjectConfiguratorTest {

    @Issue("https://github.com/eclipse/buildship/issues/794")
    def "Import triggers only one synchronization" () {
        setup:
        CountingConfigurator configurator = new CountingConfigurator()
        registerConfigurator(configurator)

        File projectDir = dir('root-project') {
             file 'build.gradle', 'apply plugin: "java"'
        }
        importAndWait(projectDir)

        expect:
        configurator.numOfSyncs == 1
    }

    class CountingConfigurator implements ProjectConfigurator {

        int numOfSyncs = 0

        @Override
        public void init(InitializationContext context, IProgressMonitor monitor) { numOfSyncs++ }

        @Override
        public void configure(ProjectContext context, IProgressMonitor monitor) { }

        @Override
        public void unconfigure(ProjectContext context, IProgressMonitor monitor) { }

    }
}
