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

package org.eclipse.buildship.core.event;

/**
 * EventBroker interface to have a wrapper in order to change the underlying event broker. The API
 * is already prepared for the IEventBroker in Eclipse 4.
 *
 * @see org.eclipse.buildship.core.event.internal.GuavaEventBroker
 */
public interface EventBroker {

    boolean post(String topic, Object data);

    boolean send(String topic, Object data);

    boolean subscribe(String topic, Object eventHandler);

    boolean unsubscribe(Object eventHandler);
}
