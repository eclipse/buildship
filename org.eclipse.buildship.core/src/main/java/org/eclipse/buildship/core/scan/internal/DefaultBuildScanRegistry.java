/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.scan.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.scan.BuildScanRegistry;

public final class DefaultBuildScanRegistry implements BuildScanRegistry {

    private final Map<ProcessDescription, String> buildScans = Collections.synchronizedMap(new HashMap<ProcessDescription, String>());
    private List<BuildScanRegistryListener> listeners = Collections.synchronizedList(new LinkedList<BuildScanRegistryListener>());

    @Override
    public void add(String buildScanUrl, ProcessDescription process) {
        this.buildScans.put(process, buildScanUrl);
        for (BuildScanRegistryListener listener : this.listeners) {
            listener.onBuildScanAdded(buildScanUrl, process);
        }
    }

    @Override
    public Optional<String> get(ProcessDescription process) {
        return Optional.fromNullable(this.buildScans.get(process));
    }

    @Override
    public void addListener(BuildScanRegistryListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(BuildScanRegistryListener listener) {
        this.listeners.remove(listener);
    }

}
