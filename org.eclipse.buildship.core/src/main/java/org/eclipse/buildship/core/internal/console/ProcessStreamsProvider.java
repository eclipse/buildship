/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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

}
