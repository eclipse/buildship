/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.eclipse;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.core.runtime.Platform;

/**
 * Provides convenience calculations related to the Eclipse platform.
 */
public class PlatformUtils {
    public static boolean supportsTestAttributes() {
        Bundle platformBundle = Platform.getBundle("org.eclipse.platform");
        if (platformBundle == null) {
            return false;
        }
        Version platform = platformBundle.getVersion();
        Version eclipseLuna = new Version(4, 8, 0);
        return platform.compareTo(eclipseLuna) >= 0;
    }
}
