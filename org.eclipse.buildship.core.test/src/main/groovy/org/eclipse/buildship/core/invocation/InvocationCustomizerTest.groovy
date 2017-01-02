package org.eclipse.buildship.core.invocation

import spock.lang.Specification

import org.eclipse.buildship.core.util.configuration.FixedRequestAttributesBuilder
import org.eclipse.buildship.core.util.extension.InvocationCustomizerCollector

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
       new InvocationCustomizerCollector().extraArguments == EXTRA_ARGUMENTS
    }

    def "FixedRequestAttributesBuilder receive extra arguments"() {
        expect:
        FixedRequestAttributesBuilder.fromWorkspaceSettings(new File('/')).build().arguments == EXTRA_ARGUMENTS
        FixedRequestAttributesBuilder.fromEmptySettings(new File('/')).build().arguments == []
    }
}
