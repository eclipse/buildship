package org.eclipse.buildship.core.internal.util.string

import spock.lang.Specification

class StringUtilsTest extends Specification {

    def "removeAdjacentDuplicates"() {
        when:
        def elements = ['bye', 'hello']
        def result = StringUtils.removeAdjacentDuplicates(elements)

        then:
        result == ['bye', 'hello']

        when:
        elements = ['hello', 'hello', 'hello', 'bye']
        result = StringUtils.removeAdjacentDuplicates(elements)

        then:
        result == ['hello','bye']

        when:
        elements = ['bye', 'hello', 'hello', 'hello', 'bye']
        result = StringUtils.removeAdjacentDuplicates(elements)

        then:
        result == ['bye', 'hello', 'bye']

        when:
        elements = ['bye', 'hello', 'hello', 'hello']
        result = StringUtils.removeAdjacentDuplicates(elements)

        then:
        result == ['bye', 'hello']
    }

}
