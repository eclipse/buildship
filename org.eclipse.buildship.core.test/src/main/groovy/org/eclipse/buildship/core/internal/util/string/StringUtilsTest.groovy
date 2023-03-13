/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
