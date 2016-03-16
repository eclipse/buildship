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
     * Returns the set of strings from the resource.
     *
     * @return the set of strings
     */
    public Set<String> get() {
        ProjectScope projectScope = new ProjectScope(this.project);
        IEclipsePreferences node = projectScope.getNode(CorePlugin.PLUGIN_ID);
        String valueString = node.get(this.propertyName, "");
        return valueString.equals("") ? ImmutableSet.<String>of() : ImmutableSet.copyOf(Splitter.on(',').split(valueString));
    }

    /**
     * Replaces the entries with the given new set.
     *
     * @param entries the new entries of this property
     */
    public void set(Set<String> entries) {
        ProjectScope projectScope = new ProjectScope(this.project);
        IEclipsePreferences node = projectScope.getNode(CorePlugin.PLUGIN_ID);
        if (entries.isEmpty()) {
            node.remove(this.propertyName);
        } else {
            String updateString = Joiner.on(',').join(entries);
            node.put(this.propertyName, updateString);
        }
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
