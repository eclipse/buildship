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
 * Dispatches {@link Event} instances to all registered {@link EventListener} instances.
 */
public interface ListenerRegistry {

    /**
     * Registers the given event listener.
     *
     * @param listener the listener to register
     */
    void addEventListener(EventListener listener);

    /**
     * Unregisters the given event listener.
     *
     * @param listener the listener to unregister
     */
    void removeEventListener(EventListener listener);

    /**
     * Dispatches the given event to all registered listeners.
     *
     * @param event the event to dispatch
     */
    void dispatch(Event event);

}
