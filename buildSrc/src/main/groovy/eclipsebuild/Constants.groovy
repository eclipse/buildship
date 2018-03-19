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

import org.gradle.api.Project;

import org.gradle.internal.os.OperatingSystem

/**
 * Contains all constants which are independent from the configuration.
 */
class Constants {

    /**
     * Returns the group name of all tasks that contribute to the Eclipse Plugin build.
     *
     * @return the name of the group where all tasks defined in this project should show upon the execution of <code>gradle tasks</code>
     */
    static String getGradleTaskGroupName() {
        return "Eclipse Plugin Build"
    }

    /**
     * Returns the Eclipse runtime abbreviation of the operating system.
     *
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return the operating system: 'linux', 'win32', 'macosx', or null
     */
    static String getOs() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? 'linux' : os.isWindows() ? 'win32' : os.isMacOsX() ? 'macosx': null
    }

    /**
     * Return the Eclipse runtime abbreviation of the windowing system.
     *
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return the windowing system: 'gtk', 'win32', 'cocoa', or null
     */
    static String getWs() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? 'gtk' : os.isWindows() ? 'win32' : os.isMacOsX() ? 'cocoa' : null
    }

    /**
     * Returns the Eclipse runtime abbreviation of the architecture.
     *
     * http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html
     *
     * @return the architecture: x86_64 or x86
     */
    static String getArch() {
        System.getProperty("os.arch").contains("64") ? "x86_64" : "x86"
    }

    /**
     * Returns the group ID of the mavenized Eclipse plugins.
     *
     * @return the group ID referencing Eclipse plugins of the mavenized target platform
     */
    static String getMavenizedEclipsePluginGroupName() {
        return "eclipse"
    }

    /**
     * Returns the OS-specific part of the path of the Eclipse executable.
     *
     * @return the OS-specific part of the path of the Eclipse executable, or null
     */
    static String getEclipseExePath() {
        OperatingSystem os = OperatingSystem.current()
        os.isLinux() ? "eclipse/eclipse" :
                os.isWindows() ? "eclipse/eclipse.exe" :
                os.isMacOsX() ? "eclipse/Eclipse.app/Contents/MacOS/eclipse" :
                null
    }

    /**
     * Returns the URL from where the Eclipse SDK (4.4.2) can be downloaded. The URL always redirects to a mirror from
     * where the Eclipse SDK can be downloaded.
     *
     * @return the URL from where the Eclipse SDK can be downloaded
     */
    static String getEclipseSdkDownloadUrl() {
        def currentOs = getOs() == 'win32' ? 'windows' : getOs()
        def currentArch = getArch() == "x86_64" ? "64" : "32";
        def fileFormat = getOs() == 'win32' ? 'zip' : 'tar.gz'
        return "http://builds.gradle.org:8000/eclipse/sdk/eclipse-sdk-4.4.2-${currentOs}-${currentArch}.${fileFormat}"
    }

    /**
     * Sets some constants in the target project's build script.
     */
    static exposePublicConstantsFor(Project project) {
        project.ext.ECLIPSE_OS = os
        project.ext.ECLIPSE_WS = ws
        project.ext.ECLIPSE_ARCH = arch
    }

}
