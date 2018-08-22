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

package org.eclipse.buildship.core.internal.event.impl;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.event.ListenerRegistry;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ListenerRegistry}.
 */
public final class DefaultListenerRegistry implements ListenerRegistry {

    private final Object LOCK = new Object();
    private final Set<EventListener> listeners = new LinkedHashSet<EventListener>();

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
            listener.onEvent(event);
        }
    }

}
