/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;

/**
 * Interface to load and store Gradle model elements stored in the workspace plugin state area. The
 * model elements can be stored and retrieved in either a structured or in a map-like fashion.
 *
 * @author Donat Csikos
 */
public interface PersistentModel {

    /**
     * Returns the build directory.
     * @return the build directory
     */
    String getBuildDir();

    /**
     * Sets the build directory.
     */
    void setBuildDir(String buildDir);

    /**
     * Returns subproject paths.
     *
     * @return the subproject paths
     */
    Collection<IPath> getSubprojectPaths();

    /**
     * Sets the subproject paths.
     *
     * @param subprojects the subproject paths.
     */
    void setSubprojectPaths(Collection<IPath> subprojectPaths);

    /**
     * Returns a value for the specified key.
     *
     * @param key the key
     * @param defaultValue to be returned if no value is present
     * @return the value
     */
    String getValue(String key, String defaultValue);

    /**
     * Stores a new key-value pair in this instance.
     *
     * @param key the key
     * @param value the value
     */
    void setValue(String key, String value);

    /**
     *  Returns a collection for the specified key.
     *
     * @param key the key
     * @param defaultValues to be returned if no value is present
     * @return the collection
     */
    Collection<String> getValues(String key, Collection<String> defaultValues);

    /**
     * Stores a new key-value pair in this instance where the value is a collection.
     *
     * @param key the key
     * @param values the values
     */
    void setValues(String key, Collection<String> values);
}
