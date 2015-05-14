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

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

/**
 * This marker interface is used for events, which contain a {@link BuildLaunchRequest} as element.
 *
 */
public interface BuildLaunchRequestEvent extends GradleEvent<BuildLaunchRequest> {

    String getProcessName();

    void setProcessName(String processName);
}
