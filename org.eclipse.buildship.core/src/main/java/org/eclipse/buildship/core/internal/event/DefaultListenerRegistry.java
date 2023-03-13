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

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.internal.CorePlugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ListenerRegistry}.
 */
public final class DefaultListenerRegistry implements ListenerRegistry {

    private final Object LOCK = new Object();
    private final Set<EventListener> listeners = new LinkedHashSet<>();

    @Override
    public void addEventListener(EventListener listener) {
        synchronized (this.LOCK) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeEventListener(EventListener listener) {
        synchronized (this.LOCK) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public void dispatch(Event event) {
        List<EventListener> listeners;
        synchronized (this.LOCK) {
            listeners = ImmutableList.copyOf(this.listeners);
        }
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                CorePlugin.logger().warn("Listener " + listener.getClass().getName() + " failed to handle " + event.getClass().getName(), e);
            }
        }
    }
}
