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

import com.gradleware.tooling.toolingclient.Request;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.launch.ExecuteLaunchRequestEvent;

/**
 * Default implementation of {@link ExecuteLaunchRequestEvent}.
 */
public final class DefaultExecuteLaunchRequestEvent implements ExecuteLaunchRequestEvent {

    private final Request<Void> request;
    private ProcessDescription processDescription;

    public DefaultExecuteLaunchRequestEvent(Request<Void> request, ProcessDescription processDescription) {
        this.request = Preconditions.checkNotNull(request);
        this.processDescription = processDescription;
    }

    @Override
    public Request<Void> getRequest() {
        return this.request;
    }

    @Override
    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }

}
