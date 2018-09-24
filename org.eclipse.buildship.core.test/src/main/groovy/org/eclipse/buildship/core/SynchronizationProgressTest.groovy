package org.eclipse.buildship.core

import org.eclipse.core.runtime.IProgressMonitor

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationProgressTest extends ProjectSynchronizationSpecification {

   def "Null monitor can be used when no progress is desired"() {
       setup:
       File location = dir('SynchronizationProgressTest')

       when:
       GradleBuild gradleBuild = gradleBuildFor(location)
       SynchronizationResult result = gradleBuild.synchronize(null)

       then:
       result.status.isOK()

       when:
       gradleBuild = gradleBuildFor(findProject('SynchronizationProgressTest'))
       result = gradleBuild.synchronize(null)

       then:
       result.status.isOK()
   }

   def "Progress reported to the monitor"() {
       setup:
       File location = dir('SynchronizationProgressTest')
       GradleBuild gradleBuild = gradleBuildFor(location)
       IProgressMonitor monitor = Mock(IProgressMonitor)

       when:
       gradleBuild.synchronize(monitor)

       then:
       (10.._) * monitor.internalWorked(_)
   }
}
