/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.internal.launch.impl;

import org.gradle.tooling.LongRunningOperation;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.launch.ExecuteLaunchRequestEvent;

/**
 * Default implementation of {@link ExecuteLaunchRequestEvent}.
 */
public final class DefaultExecuteLaunchRequestEvent implements ExecuteLaunchRequestEvent {
    private final ProcessDescription processDescription;
    private final LongRunningOperation operation;

    public DefaultExecuteLaunchRequestEvent(ProcessDescription processDescription, LongRunningOperation operation) {
        this.processDescription =  Preconditions.checkNotNull(processDescription);
        this.operation = Preconditions.checkNotNull(operation);
    }

    @Override
    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }

    @Override
    public LongRunningOperation getOperation() {
        return this.operation;
    }

}
