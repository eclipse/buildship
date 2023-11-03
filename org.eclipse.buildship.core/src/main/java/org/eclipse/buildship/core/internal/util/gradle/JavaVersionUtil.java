/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import org.gradle.api.JavaVersion;

/**
 * Utility class to map Gradle {@link JavaVersion} values to the Eclipse domain.
 *
 * @author Donat Csikos
 */
public final class JavaVersionUtil {

    private JavaVersionUtil() {
    }

    public static String adaptVersionToEclipseNamingConversions(JavaVersion javaVersion) {
        return javaVersion.isJava9Compatible() ? javaVersion.getMajorVersion() : javaVersion.toString();
    }
}
