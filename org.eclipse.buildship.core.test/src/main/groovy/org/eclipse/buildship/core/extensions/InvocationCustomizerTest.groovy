package org.eclipse.buildship.core.extensions

import spock.lang.Specification

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder

class InvocationCustomizerTest extends Specification {

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
        CorePlugin.contributionManager().contributedExtraArguments == EXTRA_ARGUMENTS
    }

    def "FixedRequestAttributesBuilder receive extra arguments"() {
        expect:
        FixedRequestAttributesBuilder.fromWorkspaceSettings(new File('/')).build().arguments == EXTRA_ARGUMENTS
        FixedRequestAttributesBuilder.fromEmptySettings(new File('/')).build().arguments == []
    }
}
