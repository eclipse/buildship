/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild

import org.gradle.internal.os.OperatingSystem


/**
 * Constant store for the Eclipse build
 */
class Constants {

    /**
     * @return The name of the group where all tasks defined in this project should show upon the execution of
     * <code>gradle tasks</code>.
     */
    static String getGradleTaskGroupName() {
        return "Eclipse Plugin Build"
    }

    /**
     * Eclipse runtime abbreviation of the operating system.
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return The operating system: 'linux', 'win32' or 'macosx'
     */
    static String getOs() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? 'linux' : os.isWindows() ? 'win32' : os.isMacOsX() ? 'macosx': null
    }

    /**
     * Eclipse runtime abbreviation of the windowing system.
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return The windowing system: 'gtk', 'win32' or 'cocoa'
     */
    static String getWs() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? 'gtk' : os.isWindows() ? 'win32' : os.isMacOsX() ? 'cocoa' : null
    }

    /**
     * Eclipse runtime abbreviation of the architecture.
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return The architecture: x86, x86_64 or ppc
     */
    static String getArch() {
        System.getProperty("os.arch").contains("64") ? "x86_64" : "x86"
    }

    /**
     * @return The list of Eclipse versions supported by this Eclipse build. Possible values: '37', '42', '43' or '44',
     */
    static List<String> getAcceptedEclipseVersions() {
        return Arrays.asList("36", "37", "42", "43", "44", "45" );
    }

    /**
     * @return The group ID for referencing eclipse bundles in the local Maven repository.
     */
    static String getMavenEclipsePluginGroupName() {
        return "eclipse"
    }

    /**
     * @return The subpath of the Eclipse executable for the current platform.
     */
    static String getEclipseExePath() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? "eclipse/eclipse" :
                os.isWindows() ? "eclipse/eclipse.exe" :
                os.isMacOsX() ? "eclipse/Eclipse.app/Contents/MacOS/eclipse" :
                null
    }

    /**
     * @return A URL which always redirect to a mirror from where and Eclipse SDK can be downloaded.
     */
    static URL getEclipseSdkDownloadUrl() {
        def archInUrl = getArch() == "x86_64" ? "-x86_64" : "";
        if (getOs() == "win32") {
            return new URL("http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.4.2-201502041700/eclipse-SDK-4.4.2-win32${archInUrl}.zip&r=1")
        }
        else {
            return new URL("http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.4.2-201502041700/eclipse-SDK-4.4.2-${getOs()}-${getWs()}${archInUrl}.tar.gz&r=1");
        }
    }
}
