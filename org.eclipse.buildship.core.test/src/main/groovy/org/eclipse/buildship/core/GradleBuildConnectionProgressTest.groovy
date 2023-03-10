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

import java.util.function.Function

import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject

import org.eclipse.core.runtime.IProgressMonitor

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleBuildConnectionProgressTest extends ProjectSynchronizationSpecification {

    def "Null monitor can be used when no progress is desired"() {
       setup:
       File location = dir('GradleBuildConnectionProgressTest')

       when:
       GradleBuild gradleBuild = gradleBuildFor(location)
       Function query = { ProjectConnection c -> c.model(GradleProject).get() }
       GradleProject model = gradleBuild.withConnection(query, null)

       then:
       model
    }

    def "Progress is logged to the monitor"() {
        setup:
        File location = dir('GradleBuildConnectionProgressTest')
        IProgressMonitor monitor = Mock(IProgressMonitor)

        when:
        GradleBuild gradleBuild = gradleBuildFor(location)
        Function query = { ProjectConnection c -> c.model(GradleProject).get() }
        GradleProject model = gradleBuild.withConnection(query, monitor)

        then:
        (10.._) * monitor./(internalW|w)orked/(_)
    }
}
