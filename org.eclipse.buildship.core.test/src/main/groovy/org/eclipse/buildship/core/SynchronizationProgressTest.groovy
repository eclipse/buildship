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

import org.eclipse.core.runtime.IProgressMonitor

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationProgressTest extends ProjectSynchronizationSpecification {

   def "Null monitor can be used when no progress is desired"() {
       setup:
       File location = dir('SynchronizationProgressTest')  {
           file 'settings.gradle', ''
       }

       when:
       GradleBuild gradleBuild = gradleBuildFor(location)
       SynchronizationResult result = gradleBuild.synchronize(null)

       then:
       assertResultOkStatus(result)

       when:
       gradleBuild = gradleBuildFor(findProject('SynchronizationProgressTest'))
       result = gradleBuild.synchronize(null)

       then:
       assertResultOkStatus(result)
   }

   def "Progress reported to the monitor"() {
       setup:
       File location = dir('SynchronizationProgressTest')  {
           file 'settings.gradle', ''
       }
       GradleBuild gradleBuild = gradleBuildFor(location)
       IProgressMonitor monitor = Mock(IProgressMonitor)

       when:
       gradleBuild.synchronize(monitor)

       then:
       (10.._) * monitor./(internalW|w)orked/(_)
   }
}
