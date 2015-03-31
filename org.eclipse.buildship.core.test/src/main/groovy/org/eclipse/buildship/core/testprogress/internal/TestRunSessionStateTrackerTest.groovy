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

import spock.lang.Specification

class TestRunSessionStateTrackerTest extends Specification {

    def "Initial state is not running"() {
        setup:
        def stateContainer = new TestRunSessionStateTracker();

        expect:
        stateContainer.currentState == TestRunSessionStateTracker.TestRunSessionState.UNKNOWN
        !stateContainer.isRunning()
    }

    def "State can be changed to running"() {
        setup:
        def stateContainer = new TestRunSessionStateTracker();

        when:
        stateContainer.sessionStarted()

        then:
        stateContainer.currentState == TestRunSessionStateTracker.TestRunSessionState.RUNNING
        stateContainer.isRunning()
    }

    def "State can be changed to finished"() {
        setup:
        def stateContainer = new TestRunSessionStateTracker();

        when:
        stateContainer.sessionFinished()

        then:
        stateContainer.currentState == TestRunSessionStateTracker.TestRunSessionState.FINISHED
        !stateContainer.isRunning()
    }

}
