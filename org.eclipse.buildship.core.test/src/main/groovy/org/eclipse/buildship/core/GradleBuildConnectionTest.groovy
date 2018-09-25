package org.eclipse.buildship.core

import java.util.function.Function

import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject

import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class GradleBuildConnectionTest extends ProjectSynchronizationSpecification {

    def "Cannot run null action"() {
        when:
        GradleBuild gradleBuild = gradleBuildFor(dir('GradleBuildConnectionTest'))
        gradleBuild.withConnection(null, new NullProgressMonitor())

        then:
        thrown NullPointerException
    }

   def "Can query a model"() {
       setup:
       File location = dir('GradleBuildConnectionTest')

       when:
       GradleBuild gradleBuild = gradleBuildFor(location)
       Function query = { ProjectConnection c -> c.model(GradleProject).get() }
       GradleProject model = gradleBuild.withConnection(query, new NullProgressMonitor())

       then:
       model.projectDirectory == location.canonicalFile
   }
}
