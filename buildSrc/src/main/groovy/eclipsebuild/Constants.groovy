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
        def classifier = "${getOs()}.${getArch()}"
        switch (classifier) {
            case "win32.x86":     return 'https://repo.gradle.org/gradle/list/ext-releases-local/org/eclipse/eclipse-sdk/4.4.2/eclipse-SDK-4.4.2-win32.zip'
            case "win32.x86_64":  return 'https://repo.gradle.org/gradle/list/ext-releases-local/org/eclipse/eclipse-sdk/4.4.2/eclipse-SDK-4.4.2-win32-x86_64.zip'
            case "linux.x86" :    return 'https://repo.gradle.org/gradle/list/ext-releases-local/org/eclipse/eclipse-sdk/4.4.2/eclipse-SDK-4.4.2-linux-gtk.tar.gz'
            case "linux.x86_64":  return 'https://repo.gradle.org/gradle/list/ext-releases-local/org/eclipse/eclipse-sdk/4.4.2/eclipse-SDK-4.4.2-linux-gtk-x86_64.tar.gz'
                case "macosx.x86_64": return 'https://repo.gradle.org/gradle/list/ext-releases-local/org/eclipse/eclipse-sdk/4.4.2/eclipse-SDK-4.4.2-macosx-cocoa-x86_64.tar.gz'
            default: throw new RuntimeException("Unsupported platform: $classifier")
        }
    }

    static String getEclipseSdkDownloadSha256Hash() {
        def classifier = "${getOs()}.${getArch()}"
        switch (classifier) {
            case "win32.x86":     return '82f0f7239eb4b638557a439a2af9ba2d6b8c846243043615b16be159ec229da6'
            case "win32.x86_64":  return 'f4db0f6cbc4e837362dd51daf7cc9662d89f6b37395f3632a19c1a6ddb6d62f3'
            case "linux.x86" :    return '27124cc182dad0a2cba76aba95788e209776b17cf842df473eb143c8a5f44cc1'
            case "linux.x86_64":  return '14a5f79fb9362993fb11ca616cde822bcfdd5daa20c3496c9d4ab91e3555003c'
            case "macosx.x86_64": return 'e49cc9b6379a4eed7613f997b0b4c415f34bb858069a134f8ad46b1585761395'
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
