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

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.event.Event;

/**
 * Event informing that a launch request is about to be executed. The request can still be modified
 * by the recipients of this event.
 */
public interface ExecuteLaunchRequestEvent extends Event {

    ProcessDescription getProcessDescription();

    LongRunningOperation getOperation();

}
