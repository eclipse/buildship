/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
