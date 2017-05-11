/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.scan;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.console.ProcessDescription;

/**
 * Contains build scans published in the current session.
 *
 * @author Donat Csikos
 */
public interface BuildScanRegistry {

    public interface BuildScanRegistryListener {

        /**
         * Callback for new build scan addition.
         *
         * @param buildScanUrl the URL of the build scan
         * @param process the process the scan belongs to
         */
        void onBuildScanAdded(String buildScanUrl, ProcessDescription process);
    }

    /**
     * Registers a new build scan.
     *
     * @param buildScanUrl the build scan url
     * @param process the process the scan belongs to
     */
    void add(String buildScanUrl, ProcessDescription process);

    /**
     * Looks up a build scan
     *
     * @param process the process the scan belongs to
     * @return the build can or {@link Optional#absent()} if none found
     */
    Optional<String> get(ProcessDescription process);

    /**
     * Adds a listener to get notified when a new scan is added to the registry.
     *
     * @param listener the listener to add
     */
    void addListener(BuildScanRegistryListener listener);

    /**
     * Removes a target listener from the registry.
     *
     * @param listener the listener to remove
     */
    void removeListener(BuildScanRegistryListener listener);
}
