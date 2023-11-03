/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
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
            // the bundle "org.eclipse.platform" will be null when it's JDT.LS
            // in that case we check the JDT.LS bundle
            return supportsTestAttributesInJdtLs();
        }
        Version platform = platformBundle.getVersion();
        Version eclipseLuna = new Version(4, 8, 0);
        return platform.compareTo(eclipseLuna) >= 0;
    }

    private static boolean supportsTestAttributesInJdtLs() {
        Bundle lsBundle = Platform.getBundle("org.eclipse.jdt.ls.core");
        if (lsBundle == null) {
            return false;
        }
        return lsBundle.getVersion().compareTo(new Version(1, 0, 0)) >= 0;
    }
}
