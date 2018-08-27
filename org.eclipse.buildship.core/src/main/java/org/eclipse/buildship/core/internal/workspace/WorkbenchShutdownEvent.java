/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
