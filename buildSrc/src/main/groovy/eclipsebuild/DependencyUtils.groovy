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

class DependencyUtils {

    static String calculatePluginDependency(Project project, String pluginName, String minimumVersion = '') {
        // if the target platform defines a version in the versionMapping
        // for the argument it returns eclipse:pluginName:versionNumber
        // otherwise it returns eclipse:pluginName:+
        def mappedVersion = mappedVersion(project, pluginName)
        def version = mappedVersion == null ? minVersion(pluginName, minimumVersion) : mappedVersion
        project.logger.debug("Plugin $pluginName mapped to version $version")
        "${Constants.mavenizedEclipsePluginGroupName}:${pluginName}:${version}"
    }

    static String minVersion(pluginName, version) {
        //  substitute Guava [27.0.0;28.0.0) OSGi dependency with version 27.1
        if (pluginName == 'com.google.guava' && version == "27.0.0") {
            "27.+"
        } else {
            "${version}+"
        }
    }

    static String mappedVersion(Project project, String pluginName) {
        Config config = Config.on(project)
        config.targetPlatform.versionMapping[pluginName]
    }
}
