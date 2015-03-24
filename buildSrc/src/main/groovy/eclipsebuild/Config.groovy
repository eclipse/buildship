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


class Config {

    Project project

    static Config on(Project project) {
        return new Config(project);
    }

    public Config(Project project) {
        this.project = project
    }

    String getEclipseVersion() {
        String version = project.hasProperty("eclipse.version") ? project.property("eclipse.version") : project.rootProject.eclipseBuild.eclipseVersion
        if (version == null) {
            throw new RuntimeException('No Eclipse version specified')
        }
        else {
            version
        }
    }

    File getContainerDir() {
        if (project.hasProperty("containerDir")) {
            return new File(project.property("containerDir"))
        }
        else {
            return new File(System.getProperty("user.home"), ".tooling/eclipse/targetPlatforms")
        }
    }

    File getEclipseSdkDir() {
        project.file("${getContainerDir()}/eclipse-sdk");
    }

    File getEclipseSdkExe() {
        project.file("${getEclipseSdkDir()}/${Constants.eclipseExePath}")
    }

    File getTargetPlatformDir() {
        return project.file("${getContainerDir()}/${getEclipseVersion()}/target-platform");
    }

    File getMavenizedTargetPlatformDir() {
        return project.file("${getContainerDir()}/${getEclipseVersion()}/mavenized-target-platform");
    }

}
