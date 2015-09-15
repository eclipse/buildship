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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Class defining how to store a set of strings on an {@link IResource} instance using persistent
 * properties.
 *
 * @see IResource#getPersistentProperties()
 */
public final class StringSetPersistentProperty {

    private final QualifiedName key;
    private final IResource resource;

    private StringSetPersistentProperty(QualifiedName key, IResource resource) {
        this.key = Preconditions.checkNotNull(key);
        this.resource = Preconditions.checkNotNull(resource);
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
     * @param entry
     */
    public void remove(String entry) {
        Set<String> current = get();
        Set<String> updated = new HashSet<String>(current);
        updated.remove(entry);
        set(updated);
    }

    /**
     * Returns the set of strings from the resource
     *
     * @return the string set
     */
    public Set<String> get() {
        try {
            String valueString = Optional.fromNullable(this.resource.getPersistentProperty(this.key)).or("");
            return ImmutableSet.copyOf(Splitter.on(',').split(valueString));
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private void set(Set<String> entries) {
        try {
            String updateString = Joiner.on(',').join(entries);
            this.resource.setPersistentProperty(this.key, updateString);
        } catch (CoreException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    /**
     * Creates a new {@link StringSetPersistentProperty} instance.
     *
     * @param key the persistent property key to define where to store the list
     * @param resource the target resource the list is associated with
     * @return the new instance
     */
    public static StringSetPersistentProperty from(QualifiedName key, IResource resource) {
        return new StringSetPersistentProperty(key, resource);
    }

}
