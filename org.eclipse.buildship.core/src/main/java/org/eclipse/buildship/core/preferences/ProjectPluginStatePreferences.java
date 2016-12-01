/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

/**
 * Interface to read and write project preferences stored in the workspace plugin state area.
 *
 * @author Donat Csikos
 */
public interface ProjectPluginStatePreferences {

    /**
     * Returns a value for the specified preference key.
     *
     * @param key the preference key
     * @param defaultValue the value to be returned if the key is not present
     * @return the preference value
     */
    String getValue(String key, String defaultValue);

    /**
     * Stores a new preference key-value pair in this instance. To persist the update, the
     * {@link #flush()} method should be called.
     *
     * @param key the key
     * @param value the value
     */
    void setValue(String key, String value);

    /**
     * Persists all changes to the disk.
     */
    void flush();
}
