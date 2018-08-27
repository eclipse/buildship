/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.internal.util.preference;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Utility methods to work with Eclipse preferences.
 */
public final class EclipsePreferencesUtils {

    private EclipsePreferencesUtils() {
    }

    /**
     * Returns the instance scope preferences object.
     * <p/>
     * In older Eclipse versions the {@code InstanceScope.INSTANCE} object does not exist. On the
     * other hand in Eclipse 4.5 the constructor of the {@code EclipseScope} class became
     * deprecated. This method returns the {@code InstanceScope} reference in a backward-compatible
     * way.
     * 
     * @return the instance scope preferences object
     */
    public static InstanceScope getInstanceScope() {
        try {
            Field field = InstanceScope.class.getField("INSTANCE");
            return (InstanceScope) field.get(null);
        } catch (Exception e1) {
            try {
                return InstanceScope.class.newInstance();
            } catch (Exception e2) {
                throw new GradlePluginsRuntimeException(e2);
            }
        }
    }
}
