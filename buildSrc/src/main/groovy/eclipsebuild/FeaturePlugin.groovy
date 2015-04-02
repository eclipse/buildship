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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Gradle plug-in for building Eclipse features.
 * <p/>
 * It validates the existence of the files feature.xml, feature.properties, build.properties, and META-INF/MANIFEST.MF. It also
 * adds all files and folders that are defined by the bin.includes entry in the build.properties file to the feature jar.
 */
class FeaturePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
      // apply the Java plugin to have the life-cycle tasks
      project.plugins.apply(JavaPlugin)

      // sync jar content with the build.properties file
      PluginUtils.configureFeatureJarInput(project)

      // validate the content
      validateRequiredFilesExist(project)
    }

    static void validateRequiredFilesExist(Project project) {
        project.gradle.taskGraph.whenReady {
            // make sure the required descriptors exist
            assert project.file('build.properties').exists()
            assert project.file('META-INF/MANIFEST.MF').exists()
            assert project.file('feature.xml').exists()
            assert project.file('feature.properties').exists()
        }
    }

}
