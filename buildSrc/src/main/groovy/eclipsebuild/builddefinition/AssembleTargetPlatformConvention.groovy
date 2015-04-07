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

package eclipsebuild.builddefinition

import eclipsebuild.Config
import org.gradle.api.Project


/**
 * Fields to set for the {@link AssembleTargetPlatformTask}
 */
class AssembleTargetPlatformConvention {

    final Project project

    String sdkVersion
    String updateSites
    String features
    File nonMavenizedTargetPlatformDir
    File eclipseSdkExe

    public AssembleTargetPlatformConvention(Project project) {
        this.project = project
    }

    def getSdkVersion() {
        Config.on(project).targetPlatform.sdkVersion
    }

    def getUpdateSites() {
        Config.on(project).targetPlatform.updateSites
    }

    def getFeatures() {
        Config.on(project).targetPlatform.features
    }

    def getNonMavenizedTargetPlatformDir() {
        Config.on(project).nonMavenizedTargetPlatformDir
    }

    def getEclipseSdkExe() {
        Config.on(project).eclipseSdkExe
    }
}
