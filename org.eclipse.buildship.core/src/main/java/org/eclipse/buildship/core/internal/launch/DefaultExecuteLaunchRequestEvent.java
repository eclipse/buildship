/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import org.gradle.tooling.LongRunningOperation;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.console.ProcessDescription;

/**
 * Default implementation of {@link ExecuteLaunchRequestEvent}.
 */
final class DefaultExecuteLaunchRequestEvent implements ExecuteLaunchRequestEvent {
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
