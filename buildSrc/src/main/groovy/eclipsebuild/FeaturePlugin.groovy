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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin for building Eclipse features.
 * <p>
 * A feature project can specify its feature.xml with a very simple build script:
 * <pre>
 apply plugin: eclipsebuild.Feature

 feature {
   featureXml = file('feature.xml')
 }
 </pre>
 * <p>
 * Only thing this plugin does is basic validation if the feature.xml file and the build.properties file exists and
 * It add everything to the plugin jar what is defined in the build.properties (just line in {@link EclipsePlugin#syncJarContentWithBuildProperties(Project)})
 */
class FeaturePlugin implements Plugin<Project> {

    static class Extension {
        File featureXml
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
    }

    void configureProject(Project project) {
        // Add a 'feature' extension to configure the location of the descriptor
        project.extensions.create('feature', Extension)
        // Quick solution to make the 'jar' task available
        // TODO: remove this and implement a custom jar task
        project.plugins.apply('java')

        // make sure the descriptors exist
        assert project.file('build.properties').exists()
        assert project.file('META-INF/MANIFEST.MF').exists()

        // sync jar content with the build.properties file
        PluginUtils.syncJarContentWithBuildProperties(project)

        // assemble task does't change anything outside the buildDir folder
        project.tasks.assemble.outputs.dir project.buildDir
    }

}
