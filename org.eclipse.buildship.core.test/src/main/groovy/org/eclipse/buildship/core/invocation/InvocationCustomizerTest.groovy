package org.eclipse.buildship.core.invocation

import org.gradle.tooling.LongRunningOperation
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.build.GradleEnvironment

import org.eclipse.core.resources.IProject

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration
import org.eclipse.buildship.core.internal.invocation.BuiltInInvocationCustomizer
import org.eclipse.buildship.core.internal.invocation.InvocationCustomizerCollector
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class InvocationCustomizerTest extends ProjectSynchronizationSpecification {

    static final List<String> EXTRA_ARGUMENTS = ['-PSampleInvocationCustomizer']

    static class SampleInvocationCustomizer implements InvocationCustomizer {
        static List<String> arguments = []

        @Override
        List<String> getExtraArguments() {
            arguments
        }
    }

    def setup() {
        SampleInvocationCustomizer.arguments = EXTRA_ARGUMENTS
    }

    def cleanup() {
        SampleInvocationCustomizer.arguments = []
    }

    def "Can contribute extra arguments"() {
        expect:
        new InvocationCustomizerCollector().extraArguments == BuiltInInvocationCustomizer.BUILTIN_ARGUMENTS + EXTRA_ARGUMENTS
    }

    def "Build configuration use extra arguments"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'settings.gradle'
        }
        importAndWait(projectDir)
        LongRunningOperation operation = Mock(LongRunningOperation)
        BuildEnvironment buildEnvironment = defaultBuildEnvironment()

        when:
        BuildConfiguration buildConfiguration = createInheritingBuildConfiguration(projectDir)
        buildConfiguration.toGradleArguments().applyTo(operation, buildEnvironment)

        then:
        1 * operation.withArguments({ it.containsAll(EXTRA_ARGUMENTS) })
    }

    def "Run configuration use extra arguments"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'settings.gradle'
        }
        importAndWait(projectDir)
        IProject project = findProject('sample-project')
        LongRunningOperation operation = Mock(LongRunningOperation)
        BuildEnvironment buildEnvironment = defaultBuildEnvironment()

        when:
        CorePlugin.configurationManager().loadRunConfiguration(createGradleLaunchConfig()).toGradleArguments().applyTo(operation, buildEnvironment)

        then:
        1 * operation.withArguments({ it.containsAll(EXTRA_ARGUMENTS) })
    }

    private BuildEnvironment defaultBuildEnvironment() {
        GradleEnvironment gradleEnvironment = Mock(GradleEnvironment)
        gradleEnvironment.gradleVersion >> '3.5'
        BuildEnvironment buildEnvironment = Mock(BuildEnvironment)
        buildEnvironment.gradle >> gradleEnvironment
        buildEnvironment
    }
}
