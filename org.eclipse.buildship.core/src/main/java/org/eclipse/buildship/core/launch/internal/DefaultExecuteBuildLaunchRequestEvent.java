/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.launch.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import org.eclipse.buildship.core.launch.ExecuteBuildLaunchRequestEvent;

/**
 * Default implementation of {@link ExecuteBuildLaunchRequestEvent}.
 */
public final class DefaultExecuteBuildLaunchRequestEvent implements ExecuteBuildLaunchRequestEvent {

    private final BuildLaunchRequest request;
    private final String processName;

    public DefaultExecuteBuildLaunchRequestEvent(BuildLaunchRequest request, String processName) {
        this.request = Preconditions.checkNotNull(request);
        this.processName = Preconditions.checkNotNull(processName);
    }

    @Override
    public BuildLaunchRequest getRequest() {
        return this.request;
    }

    @Override
    public String getProcessName() {
        return this.processName;
    }

}
