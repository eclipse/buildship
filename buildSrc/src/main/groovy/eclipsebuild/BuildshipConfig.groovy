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


/**
 * Exposes configuration specific for the Buildship project.
 */
class BuildshipConfig {

    static final def VALID_RELEASE_TYPES = [
        'release',
        'milestone',
        'snapshot'
    ]

    final def project

    private BuildshipConfig(Project project) {
        this.project = project
    }

    static def on(Project project) {
        new BuildshipConfig(project)
    }

    def isRelease() {
        project.hasProperty('release.type') ? releaseType == 'release' : false
    }

    def isMilestone() {
        project.hasProperty('release.type') ? releaseType == 'milestone' : false
    }

    def isSnapshot() {
        project.hasProperty('release.type') ? releaseType == 'snapshot' : false
    }

    private def getReleaseType() {
        def releaseType = project.property('release.type')
        if (VALID_RELEASE_TYPES.contains(releaseType)) {
            releaseType
        }
        else {
            throw new IllegalArgumentException("Unsupported value for project property 'release.type': $releaseType. Supported values: $VALID_RELEASE_TYPES")
        }
    }

}
