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

package org.eclipse.buildship.core.workspace.internal;

import java.util.HashSet;
import java.util.Set;

import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Stores a set of strings associated with a {@link IProject} instance.
 */
final class StringSetProjectProperty {

    private final IProject project;
    private final String propertyName;

    private StringSetProjectProperty(IProject project, String propertyName) {
        this.project = Preconditions.checkNotNull(project);
        this.propertyName = Preconditions.checkNotNull(propertyName);
    }

    /**
     * Adds a new entry to the set.
     *
     * @param entry the entry to add
     */
    public void add(String entry) {
        Set<String> updated = ImmutableSet.<String>builder().addAll(get()).add(entry).build();
        set(updated);
    }

    /**
     * Removes an entry from the set.
     *
     * @param entry the entry to remove
     */
    public void remove(String entry) {
        Set<String> current = get();
        Set<String> updated = new HashSet<String>(current);
        updated.remove(entry);
        set(updated);
    }

    /**
     * Returns the set of strings from the resource.
     *
     * @return the set of strings
     */
    public Set<String> get() {
        ProjectScope projectScope = new ProjectScope(this.project);
        IEclipsePreferences node = projectScope.getNode(CorePlugin.PLUGIN_ID);
        String valueString = node.get(this.propertyName, "");
        return ImmutableSet.copyOf(Splitter.on(',').split(valueString));
    }

    private void set(Set<String> entries) {
        ProjectScope projectScope = new ProjectScope(this.project);
        IEclipsePreferences node = projectScope.getNode(CorePlugin.PLUGIN_ID);
        String updateString = Joiner.on(',').join(entries);
        node.put(this.propertyName, updateString);
        try {
            node.flush();
        } catch (BackingStoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    /**
     * Creates a new {@link StringSetProjectProperty} instance.
     *
     * @param project the target project that the property is associated with
     * @return the new instance
     */
    public static StringSetProjectProperty from(IProject project, String propertyName) {
        return new StringSetProjectProperty(project, propertyName);
    }

}
