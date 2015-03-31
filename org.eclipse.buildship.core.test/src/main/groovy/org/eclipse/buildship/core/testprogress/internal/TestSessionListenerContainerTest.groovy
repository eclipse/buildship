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

package org.eclipse.buildship.core.testprogress.internal

import org.eclipse.jdt.internal.junit.model.ITestSessionListener
import org.eclipse.jdt.internal.junit.model.TestCaseElement
import org.eclipse.jdt.internal.junit.model.TestElement.Status
import org.eclipse.jdt.internal.junit.model.TestSuiteElement
import spock.lang.Specification

@SuppressWarnings("restriction")
class TestSessionListenerContainerTest extends Specification {

    TestSessionListenerContainer container = new TestSessionListenerContainer()
    TestSuiteElement testSuite = new TestSuiteElement(null, "id_1", "mocked_test_suite_1", 1)
    TestCaseElement testCase = new TestCaseElement(testSuite, "id_2", "mocked_test_case_1")

    def "Listeners receive events"() {
        setup:
        ITestSessionListener listener = Mock(ITestSessionListener)
        container.add(listener)

        when:
        container.notifySessionStarted()
        then:
        1 * listener.sessionStarted()

        when:
        container.notifySessionEnded(1000)
        then:
        1 * listener.sessionEnded(1000)


        when:
        container.notifyTestingBegins()
        then:
        1 * listener.runningBegins()

        when:
        container.notifySuiteStarted(testSuite)
        then:
        1 * listener.testAdded(testSuite)

        when:
        container.notifySuiteFinished(testSuite, Status.OK, null, null, null)
        then:
        1 * listener.testFailed(testSuite,Status.OK,_,_,_)

        when:
        container.notifyTestStarted(testCase)
        then:
        1 * listener.testStarted(testCase)

        when:
        container.notifyTestEnded(testCase)
        then:
        1 * listener.testEnded(testCase)
    }

    def "Can add and remove listeners"() {
        TestSessionListenerContainer container = new TestSessionListenerContainer()
        ITestSessionListener listener = Mock(ITestSessionListener)

        when:
        container.notifySessionStarted()
        then:
        0 * listener.sessionStarted()

        when:
        container.add(listener)
        container.notifySessionStarted()
        then:
        1 * listener.sessionStarted()

        when:
        container.remove(listener)
        container.notifySessionStarted()
        then:
        0 * listener.sessionStarted()
    }
}
