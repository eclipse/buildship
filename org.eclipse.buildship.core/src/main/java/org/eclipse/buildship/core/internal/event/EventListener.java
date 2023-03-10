/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.event;

/**
 * Listens to {@link Event} instances dispatched via {@link ListenerRegistry}.
 */
public interface EventListener {

    /**
     * Invoked when an event has been dispatched through the listener registry with which this
     * listener is registered. More information about the actual event is available by checking
     * its concrete sub-type.
     *
     * @param event the dispatched event
     */
    void onEvent(Event event);

}
