/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

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
