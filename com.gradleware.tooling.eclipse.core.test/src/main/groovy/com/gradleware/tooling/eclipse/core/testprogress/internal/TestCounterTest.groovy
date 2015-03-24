/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gradleware.tooling.eclipse.core.testprogress.internal

import spock.lang.Specification;


class TestCounterTest extends Specification {

    def "Counters starts from zero"() {
        setup:
        def TestCounter counter = new TestCounter()

        expect:
        counter.getStartedCount() == 0
        counter.getFinishedCount() == 0
        counter.getFailureCount() == 0
        counter.getErrorCount() == 0
        counter.getIgnoredCount() == 0
    }

    def "Increment started"() {
        setup:
        def TestCounter counter = new TestCounter()

        when:
        counter.incrementStarted()

        then:
        counter.getStartedCount() == 1
        counter.getFinishedCount() == 0
        counter.getFailureCount() == 0
        counter.getErrorCount() == 0
        counter.getIgnoredCount() == 0
    }

    def "Increment success"() {
        setup:
        def TestCounter counter = new TestCounter()

        when:
        counter.incrementSuccess()

        then:
        counter.getStartedCount() == 0
        counter.getFinishedCount() == 1
        counter.getFailureCount() == 0
        counter.getErrorCount() == 0
        counter.getIgnoredCount() == 0
    }

    def "Increment failure"() {
        setup:
        def TestCounter counter = new TestCounter()

        when:
        counter.incrementFailure()

        then:
        counter.getStartedCount() == 0
        counter.getFinishedCount() == 1
        counter.getFailureCount() == 1
        counter.getErrorCount() == 0
        counter.getIgnoredCount() == 0
    }

    def "Increment ignored"() {
        setup:
        def TestCounter counter = new TestCounter()

        when:
        counter.incrementIgnored()

        then:
        counter.getStartedCount() == 0
        counter.getFinishedCount() == 1
        counter.getFailureCount() == 0
        counter.getErrorCount() == 0
        counter.getIgnoredCount() == 1
    }
}
