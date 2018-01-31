package org.eclipse.buildship.core.util.gradle;

import org.gradle.api.JavaVersion;

public final class JavaVersionUtil {

    private JavaVersionUtil() {
    }

    public static String adaptVersionToEclipseNamingConversions(JavaVersion javaVersion) {
        return  javaVersion.isJava9Compatible() ? javaVersion.getMajorVersion() : javaVersion.toString();
    }
}
