package org.eclipse.buildship.core

import spock.lang.Specification;

class GradleCoreTest extends Specification {

    def "All API services are available"() {
        expect:
        GradleCore.workspace.configurationManager
    }
}
