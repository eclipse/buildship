package org.eclipse.buildship.core

import java.util.function.Function

import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.eclipse.EclipseProject

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.test.fixtures.EclipseProjects

class GradleBuildConnectionCachingTest extends BaseProjectConfiguratorTest {

    def "Does not cache results when not in synchronization"() {
       setup:
       File location = dir('GradleBuildConnectionCachingTest')
       GradleBuild gradleBuild = gradleBuildFor(location)
       Function query = { ProjectConnection c -> c.getModel(GradleProject) }

       when:
       GradleProject firstModel = gradleBuild.withConnection(query, null)
       GradleProject secondModel = gradleBuild.withConnection(query, null)

       then:
       !firstModel.is(secondModel)
    }

    def "Loads value from cache during synchronization"() {
        setup:
        TestConfigurator firstConfigurator = new TestConfigurator()
        TestConfigurator secondConfigurator = new TestConfigurator()
        registerConfigurator(firstConfigurator)
        registerConfigurator(secondConfigurator)
        File location = dir('GradleBuildConnectionCachingTest') {
            file "build.gradle", "apply plugin: 'java'"
        }
        GradleBuild gradleBuild = gradleBuildFor(location)

        when:
        gradleBuild.synchronize(new NullProgressMonitor())

        then:
        firstConfigurator.eclipseProject.is(secondConfigurator.eclipseProject)
     }

     // TODO (donat) add more test coverage checking all caching scenarios: build actions, non-cacheabe setup, etc.

     static class TestConfigurator implements ProjectConfigurator {

        EclipseProject eclipseProject

        @Override
        public void init(InitializationContext context, IProgressMonitor monitor) {
            Function<ProjectConnection, EclipseProject> action = { ProjectConnection p -> p.getModel(EclipseProject.class) }
            context.gradleBuild.withConnection(action, monitor)
        }

        @Override
        public void configure(ProjectContext context, IProgressMonitor monitor) { }

        @Override
        public void unconfigure(ProjectContext context, IProgressMonitor monitor) { }
     }
}
