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
 * The plug-in uses a DSL to specify the feature.xml file:
 * <pre>
 * apply plugin: eclipsebuild.Feature
 *
 * feature {
 *     featureXml = file('feature.xml')
 * }
 * </pre>
 * It validates the existence of the feature.xml and the build.properties file. Also it adds
 * everything to the plugin jar what is defined in the build.properties.
 */
class FeaturePlugin implements Plugin<Project> {

    /**
     *  Extension class providing top-level content of the DSL definition for the plug-in.
     */
    static class Extension {
        File featureXml
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
    }

    // name of the root node in the DSL
    static String DSL_EXTENSION_NAME = "feature"

    static void configureProject(Project project) {
        // add a 'feature' extension to configure the location of the descriptor
        project.extensions.create(DSL_EXTENSION_NAME, Extension)
        project.plugins.apply(JavaPlugin)

        // sync jar content with the build.properties file
        PluginUtils.syncJarContentWithBuildProperties(project)

        // validate the content
        validateFeatureBeforeBuildStarts(project)
    }

    static void validateFeatureBeforeBuildStarts(Project project) {
        project.gradle.taskGraph.whenReady {
            // make sure the required descriptors exist
            assert project.file('build.properties').exists()
            assert project.file('META-INF/MANIFEST.MF').exists()
            assert project.file(project.feature.featureXml).exists()
        }
    }
}
