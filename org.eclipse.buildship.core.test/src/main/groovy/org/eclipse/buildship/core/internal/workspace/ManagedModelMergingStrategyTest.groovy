/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import spock.lang.Specification

import org.eclipse.buildship.core.internal.workspace.ManagedModelMergingStrategy
import org.eclipse.buildship.core.internal.workspace.ManagedModelMergingStrategy.Result

class ManagedModelMergingStrategyTest extends Specification {

    def "Verify algorithm"(current, model, managed, expectedElements, expectedManaged) {
        when:
        Result result = ManagedModelMergingStrategy.calculate(current as Set, model as Set, managed as Set)

        then:
        result.nextElements == expectedElements as Set
        result.nextManaged == expectedManaged as Set

        where:
        current | model | managed | expectedElements | expectedManaged
        [ ]     | [ ]   | [ ]     | [ ]              | [ ]
        [1]     | [ ]   | [ ]     | [1]              | [ ]
        [ ]     | [1]   | [ ]     | [1]              | [1]
        [1]     | [1]   | [ ]     | [1]              | [ ]
        [ ]     | [ ]   | [1]     | [ ]              | [1]
        [1]     | [ ]   | [1]     | [ ]              | [ ]
        [ ]     | [1]   | [1]     | [1]              | [1]
        [1]     | [1]   | [1]     | [1]              | [1]
    }
}
