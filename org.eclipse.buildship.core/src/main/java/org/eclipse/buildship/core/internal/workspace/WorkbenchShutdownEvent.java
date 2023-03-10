/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import org.eclipse.buildship.core.internal.event.Event;

/**
 * Event raised just before the workbench shuts down.
 * <p/>
 * Since the JVM will shut down shortly after this event, listeners should react synchronously.
 *
 * @author Donat Csikos
 */
public final class WorkbenchShutdownEvent implements Event {

    public WorkbenchShutdownEvent() {
    }
}
