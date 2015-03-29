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

import eclipsebuild.BuildDefinitionPlugin.TargetPlatform;

// TODO (DONAT) add class-level javadoc
class Config {

    // TODO (DONAT) make final
    Project project

    static Config on(Project project) {
        return new Config(project);
    }

    // TODO (DONAT) why not private when there is a factory method?
    public Config(Project project) {
        this.project = project
    }

    // TODO (DONAT) in general in this file and all others, use Groovy style to call getters (since we are using Groovy here)

    TargetPlatform getTargetPlatform() {
        project.rootProject.eclipseBuild.targetPlatforms[getEclipseVersion()];
    }

    String getEclipseVersion() {
      // TODO (DONAT) move this logic to the constructor and keep value in a field (and turn this method into a simple getter method)
        String version = project.hasProperty("eclipse.version") ? project.property("eclipse.version") : project.rootProject.eclipseBuild.defaultEclipseVersion
        if (version == null) {
            throw new RuntimeException('No Eclipse version specified')
        }
        else {
            version
        }
    }

    // TODO (DONAT) I would rename this to getTargetPlatformsDir
    // TODO (DONAT) I would also rename the property to targetPlatformsDir
    // TODO (DONAT) document on the BuildDefinitionPlugin that this can be configured via project property
    File getContainerDir() {
      // TODO (DONAT) move this logic to the constructor and keep value in a field (and turn this method into a simple getter method)
      if (project.hasProperty("containerDir")) {
            return new File(project.property("containerDir") as String)
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
