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

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

import org.eclipse.buildship.core.event.BuildLaunchRequestEvent;

/**
 * This is the implementation of the {@link BuildLaunchRequestEvent}.
 *
 */
public class DefaultBuildLaunchRequestEvent extends DefaultGradleEvent<BuildLaunchRequest> implements BuildLaunchRequestEvent {

	public DefaultBuildLaunchRequestEvent(Object source,
			BuildLaunchRequest element) {
		super(source, element);
	}
}
