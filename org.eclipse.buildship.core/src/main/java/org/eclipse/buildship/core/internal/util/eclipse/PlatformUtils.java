/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
