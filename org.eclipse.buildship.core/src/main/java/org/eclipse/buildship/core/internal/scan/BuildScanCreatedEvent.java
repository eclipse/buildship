/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
