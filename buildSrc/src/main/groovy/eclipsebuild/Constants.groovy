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

import org.gradle.api.Project
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
        def arch = System.getProperty("os.arch")
        if (arch == 'aarch64') {
            return arch
        } else {
            return arch.contains("64") ? "x86_64" : "x86"
        }
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
                os.isMacOsX() ? "Eclipse.app/Contents/MacOS/eclipse" :
                null
    }

    /**
     * Returns the URL from where the Eclipse SDK (4.4.2) can be downloaded. The URL always redirects to a mirror from
     * where the Eclipse SDK can be downloaded.
     *
     * @return the URL from where the Eclipse SDK can be downloaded
     */
    static String getEclipseSdkDownloadUrl() {
        def classifier = "${getOs()}.${getArch()}"
        switch (classifier) {
            // TODO update SDK for Linux and Windows builds
            case "win32.x86":      return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.8.0/eclipse-SDK-4.8-win32.zip'
            case "win32.x86_64":   return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.8.0/eclipse-SDK-4.8-win32-x86_64.zip'
            case "linux.x86" :     return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.8.0/eclipse-SDK-4.8-linux-gtk.tar.gz'
            case "linux.x86_64":   return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.8.0/eclipse-SDK-4.8-linux-gtk-x86_64.tar.gz'
            case "macosx.x86_64":  return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.8.0/eclipse-SDK-4.8-macosx-cocoa-x86_64.dmg'
            case "macosx.aarch64": return 'https://repo.gradle.org/artifactory/ext-releases-local/org/eclipse/eclipse-sdk/4.24/eclipse-SDK-4.24-macosx-cocoa-aarch64.dmg'
            default: throw new RuntimeException("Unsupported platform: $classifier")
        }
    }

    static String getEclipseSdkDownloadSha256Hash() {
        def classifier = "${getOs()}.${getArch()}"
        switch (classifier) {
            case "win32.x86":      return '5d95ba57d48e48ecce7993645e4416557696beb614ca389f9b4c296696058cf8'
            case "win32.x86_64":   return '5b2873ee265d53a4cc2b5fd56e6fd3a29cf07f49a6576f4bb29cfb49114774b3'
            case "linux.x86" :     return '606f74899eb29bb5c6072457c88f08c599cbf3dec3908d8d4bdeadaeb5aeba4e'
            case "linux.x86_64":   return '8704b5ef20e76f7e0d1c7cd2139f08b909efd0dfca19f44f1b0d5f615606b263'
            case "macosx.x86_64":  return '2d5a377c64b64dc3661b90e66c51141467ed7c7ee6235bab91ae1a569da0b422'
            case "macosx.aarch64": return 'dd8988a3d60aedc8bc21de79bc5ef299037c0da8d23bdc367ecaf9f3799340d5'
            default: throw new RuntimeException("Unsupported platform: $classifier")
        }
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
