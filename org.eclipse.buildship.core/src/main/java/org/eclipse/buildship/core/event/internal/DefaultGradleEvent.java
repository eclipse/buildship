/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.event.internal;

import org.eclipse.buildship.core.event.GradleEvent;

/**
 * This is the default implementation of a {@link GradleEvent}.
 *
 * @param <T> is the type of the element, which is passed within the event
 */
public class DefaultGradleEvent<T> implements GradleEvent<T> {

    private Object source;
    private T element;

    public DefaultGradleEvent(Object source, T element) {
        this.source = source;
        this.element = element;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public T getElement() {
        return element;
    }

}
