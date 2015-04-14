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
 * Holds Buildship-specific, configuration-dependent settings for the plug-ins.
 */
class BuildshipConfig {

    static final String RELEASE_TYPE_PROJECT_PROPERTY = 'release.type'

    static final String RELEASE = 'release'
    static final String MILESTONE = 'milestone'
    static final String SNAPSHOT = 'snapshot'

    static final def RELEASE_TYPES = [RELEASE, MILESTONE, SNAPSHOT]

    final def project

    private BuildshipConfig(Project project) {
        this.project = project
    }

    static def on(Project project) {
        new BuildshipConfig(project)
    }

    def isRelease() {
        project.hasProperty(RELEASE_TYPE_PROJECT_PROPERTY) ? releaseType == RELEASE : false
    }

    def isMilestone() {
        project.hasProperty(RELEASE_TYPE_PROJECT_PROPERTY) ? releaseType == MILESTONE : false
    }

    def isSnapshot() {
        project.hasProperty(RELEASE_TYPE_PROJECT_PROPERTY) ? releaseType == SNAPSHOT : true // by default, use as snapshot release
    }

    private def getReleaseType() {
        def releaseType = project.property(RELEASE_TYPE_PROJECT_PROPERTY)
        if (RELEASE_TYPES.contains(releaseType)) {
            releaseType
        } else {
          throw new IllegalArgumentException("Unsupported value for project property '$RELEASE_TYPE_PROJECT_PROPERTY': $releaseType. Supported values: $RELEASE_TYPES")
        }
    }

}
