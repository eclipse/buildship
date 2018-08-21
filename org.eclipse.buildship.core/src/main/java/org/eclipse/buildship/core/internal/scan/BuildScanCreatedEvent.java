/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal.scan;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.event.Event;

/**
 * Event raised when a build scan is created in a build invocation.
 *
 * @author Donat Csikos
 */
public final class BuildScanCreatedEvent implements Event {

    private final String buildScanUrl;
    private final ProcessDescription processDescription;

    public BuildScanCreatedEvent(String buildScanUrl, ProcessDescription processDescription) {
        this.buildScanUrl = Preconditions.checkNotNull(buildScanUrl);
        this.processDescription = Preconditions.checkNotNull(processDescription);
    }

    public String getBuildScanUrl() {
        return this.buildScanUrl;
    }

    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }
}
