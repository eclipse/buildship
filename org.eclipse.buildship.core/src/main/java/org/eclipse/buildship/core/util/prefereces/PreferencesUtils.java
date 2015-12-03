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

package org.eclipse.buildship.core.util.prefereces;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Utility methods to access Eclipse preferences.
 */
public final class PreferencesUtils {

    private PreferencesUtils() {
    }

    /**
     * Returns {@link InstanceScope} regardless of the IDE version.
     * <p/>
     * In older Eclipse releases a different API is defined to query preferences. The method uses
     * reflection to leverage this difference.
     */
    public static InstanceScope getInstanceScope() {
        try {
            Class<InstanceScope> clazz = InstanceScope.class;
            try {
                // for Eclipse 3.7+ use return InstanceScope.INSTANCE
                Field instance = clazz.getField("INSTANCE");
                return (InstanceScope) instance.get(null);
            } catch (NoSuchFieldException e) {
                // for older versions return new InstanceScope()
                return clazz.newInstance();
            }
        } catch (IllegalArgumentException e) {
            throw new GradlePluginsRuntimeException(e);
        } catch (SecurityException e) {
            throw new GradlePluginsRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new GradlePluginsRuntimeException(e);
        } catch (InstantiationException e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }
}
