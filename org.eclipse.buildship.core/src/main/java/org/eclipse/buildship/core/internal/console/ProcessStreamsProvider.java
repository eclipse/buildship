/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.console;

/**
 * Provider interface to obtain {@link ProcessStreams} instances.
 */
public interface ProcessStreamsProvider {

    /**
     * Returns a singleton {@link ProcessStreams} instance suited for background processes.
     *
     * @return the instance suitable for background processes
     */
    ProcessStreams getBackgroundJobProcessStreams();

    /**
     * Creates a new {@link ProcessStreams} instance.
     *
     * @param processDescription the backing process
     * @return the new instance
     */
    ProcessStreams createProcessStreams(ProcessDescription processDescription);

    /**
     * Searches the existing streams for the target description and creates a new one if none found.
     *
     * @param processDescription the backing process
     * @return the new instance
     */
    ProcessStreams getOrCreateProcessStreams(ProcessDescription processDescription);

}
