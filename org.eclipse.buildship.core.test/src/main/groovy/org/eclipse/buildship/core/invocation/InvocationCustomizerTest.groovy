package org.eclipse.buildship.core.invocation

import org.eclipse.core.resources.IProject
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchManager

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.extension.InvocationCustomizerCollector

class InvocationCustomizerTest extends ProjectSynchronizationSpecification {

    static final List<String> EXTRA_ARGUMENTS = ['-PSampleInvocationCustomizer']

    static class SampleInvocationCustomizer implements InvocationCustomizer {
        static List<String> arguments = []

        @Override
        List<String> getExtraArguments() {
            arguments
        }
    }

    void setup() {
        SampleInvocationCustomizer.arguments = EXTRA_ARGUMENTS
    }

    void cleanup() {
        SampleInvocationCustomizer.arguments = []
    }

    def "Can contribute extra arguments"() {
        expect:
        new InvocationCustomizerCollector().extraArguments == EXTRA_ARGUMENTS
    }

    def "Run configuration contains extra arguments"() {
        setup:
        File projectDir = dir('sample-project') {
            file 'settings.gradle'
        }
        synchronizeAndWait(projectDir)
        IProject project = findProject('sample-project')

        expect:
        CorePlugin.configurationManager().loadRunConfiguration(emptyLaunchConfiguration()).arguments == EXTRA_ARGUMENTS
    }

    private ILaunchConfiguration emptyLaunchConfiguration() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, "launch-config-name")
    }
}
