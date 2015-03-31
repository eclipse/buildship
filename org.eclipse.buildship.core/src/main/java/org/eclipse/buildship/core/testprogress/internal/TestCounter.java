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

package org.eclipse.buildship.core.testprogress.internal;

/**
 * Keeps track of the total number of actual tests that been started, succeeded, failed, skipped.
 */
final class TestCounter {

    int started;
    int success;
    int failure;
    int error;
    int ignored;

    int getStartedCount() {
        return this.started;
    }

    public int getFinishedCount() {
        return this.success + this.failure + this.error + this.ignored;
    }

    int getFailureCount() {
        return this.failure;
    }

    int getErrorCount() {
        return this.error;
    }

    int getIgnoredCount() {
        return this.ignored;
    }

    void incrementStarted() {
        this.started++;
    }

    void incrementSuccess() {
        this.success++;
    }

    void incrementFailure() {
        this.failure++;
    }

    void incrementIgnored() {
        this.ignored++;
    }

}
