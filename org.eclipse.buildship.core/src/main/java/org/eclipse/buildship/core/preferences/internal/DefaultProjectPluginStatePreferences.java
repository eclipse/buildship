/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.preferences.ProjectPluginStatePreferences;

/**
 * Default implementation for {@link ProjectPluginStatePreferences}.
 *
 * @author Donat Csikos
 */
class DefaultProjectPluginStatePreferences implements ProjectPluginStatePreferences {

    private final DefaultProjectPluginStatePreferenceStore store;
    private final IProject project;
    private final ImmutableMap<String, String> entries;
    private final Map<String, String> added;
    private final Set<String> removed;

    DefaultProjectPluginStatePreferences(DefaultProjectPluginStatePreferenceStore store, IProject project, Map<String, String> entries) {
        this.store = Preconditions.checkNotNull(store);
        this.project = Preconditions.checkNotNull(project);
        this.entries = ImmutableMap.copyOf(entries);
        this.added = Maps.newHashMap();
        this.removed = Sets.newHashSet();
    }

    Map<String, String> getAdded() {
        return this.added;
    }

    Set<String> getRemoved() {
        return this.removed;
    }

    IProject getProject() {
        return this.project;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        if (this.added.keySet().contains(key)) {
            return this.added.get(key);
        } else if (this.removed.contains(key)) {
            return defaultValue;
        } else {
            String value = this.entries.get(key);
            return value != null ? value : defaultValue;
        }
    }

    @Override
    public void setValue(String key, String value) {
        if (value != null) {
            this.added.put(key, value);
        } else if (this.entries.containsKey(key)) {
            this.removed.add(key);
        }
    }

    @Override
    public void flush() {
        this.store.persistPrefs(this);
        this.added.clear();
        this.removed.clear();
    }
}
