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

import eclipsebuild.BuildDefinitionPlugin.TargetPlatform


/**
 * Holds configuration-dependent settings for the plug-ins.
 */
class Config {

    private final Project project

    static Config on(Project project) {
        return new Config(project)
    }

    private Config(Project project) {
        this.project = project
    }

    TargetPlatform getTargetPlatform() {
        project.rootProject.eclipseBuild.targetPlatforms[eclipseVersion]
    }

    String getEclipseVersion() {
        // to avoid configuration timing issues we don't cache the values in fields
        project.hasProperty('eclipse.version') ?
                project.property('eclipse.version') :
                project.rootProject.eclipseBuild.defaultEclipseVersion
    }

    File getTargetPlatformsDir() {
        // to avoid configuration timing issues we don't cache the values in fields
        project.hasProperty('targetPlatformsDir') ?
                new File(project.property('targetPlatformsDir') as String) :
                new File(System.getProperty('user.home'), '.tooling/eclipse/targetPlatforms')
    }

    // the hierarchy obtainable with the API below:
    //
    //   targetPlatformsDir
    //    |--eclipse-sdk
    //    |  |--eclipse-sdk.tar.gz
    //    |  |--eclipse
    //    |     |--Eclipse.app/Contents/MacOS/eclipse
    //    |     |--plugins
    //    |     |--features
    //    |--45
    //    |  |--target-platform
    //    |  |--mavenized-target-platform
    //    |     |--version
    //    |--37
    //       |--...

    File getEclipseSdkDir() {
        new File(targetPlatformsDir, 'eclipse-sdk')
    }

    File getTargetPlatformDir() {
        new File(targetPlatformsDir, eclipseVersion)
    }

    File getNonMavenizedTargetPlatformDir() {
        new File(targetPlatformDir, 'target-platform')
    }

    File getMavenizedTargetPlatformDir() {
        new File(targetPlatformDir, 'mavenized-target-platform')
    }

    File getEclipseSdkArchive() {
        new File(eclipseSdkDir, OperatingSystem.current().isWindows() ? 'eclipse-sdk.zip' : 'eclipse-sdk.tar.gz')
    }

    File getEclipseSdkExe() {
        new File(eclipseSdkDir, Constants.eclipseExePath)
    }

    File getJarProcessorJar() {
        new File(eclipseSdkDir.path, '/eclipse/plugins').listFiles().find { it.name.startsWith('org.eclipse.equinox.p2.jarprocessor_') }
    }
}
