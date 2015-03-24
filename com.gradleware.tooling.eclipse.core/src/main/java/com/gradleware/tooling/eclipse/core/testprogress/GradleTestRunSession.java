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

package com.gradleware.tooling.eclipse.core.testprogress;

import org.gradle.tooling.TestProgressEvent;

/**
 * Decorates a {@code TestRunSession} that can be started, finished, and fed with {@code TestProgressEvent} instances that mark test execution progress.
 */
public interface GradleTestRunSession {

    /**
     * Notifies the underlying {@code TestRunSession} that a new Gradle build with potential test execution has started.
     */
    void start();

    /**
     * Notifies the underlying {@code TestRunSession} that the previously started Gradle build has finished.
     */
    void finish();

    /**
     * Converts and forwards the given test progress event to the underlying {@code TestRunSession} instance.
     *
     * @param event the event to process
     */
    void process(TestProgressEvent event);

}
