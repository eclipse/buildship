/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.launch

import org.eclipse.debug.core.ILaunchConfiguration

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.ui.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleLaunchConfigurationTest extends ProjectSynchronizationSpecification {

    def "Launch configuration can be loaded and validated after the project is deleted"() {
        setup:
        def project = dir("root") {
            file 'build.gradle'
            file 'settings.gradle'
        }
        importAndWait(project)
        def attributes = attributes('${workspace_loc:/root}')
        ILaunchConfiguration configuration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(attributes)

        when:
        deleteAllProjects(true)
        attributes = GradleRunConfigurationAttributes.from(configuration)
        attributes.getWorkingDirExpression()

        then:
        notThrown(Exception)

        when:
        attributes.getWorkingDir()

        then:
        thrown(Exception)
    }

    private GradleRunConfigurationAttributes attributes(String projectLoc) {
        new GradleRunConfigurationAttributes([],
            projectLoc,
            GradleDistribution.fromBuild().toString(),
            "",
            null,
            [],
            [],
            true,
            true,
            false,
            false,
            false);
    }

}
