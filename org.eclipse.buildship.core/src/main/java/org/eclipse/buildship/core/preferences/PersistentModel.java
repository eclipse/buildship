/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

/**
 * Interface to load and store Gradle model elements stored in the workspace plugin state area. The
 * model elements are stored and retrieved in a map-like fashion.
 *
 * @author Donat Csikos
 */
public interface PersistentModel {

    public static final String PROPERTY_SUBPROJECTS = "subprojects";
    public static final String PROPERTY_BUILD_DIR = "buildDir";

    /**
     * Returns a value for the specified key.
     *
     * @param key the key
     * @param defaultValue the to be returned if no value is present
     * @return the value
     */
    String getValue(String key, String defaultValue);

    /**
     * Stores a new key-value pair in this instance. To persist the update, the {@link #flush()}
     * method should be called.
     *
     * @param key the key
     * @param value the value
     */
    void setValue(String key, String value);
}
