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

    // TODO (DONAT) class-level javadoc
    static class Extension {
        File featureXml
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
    }

  static void configureProject(Project project) {
        // Add a 'feature' extension to configure the location of the descriptor
        project.extensions.create('feature', Extension)  // TODO (DONAT) extract constant (do similarly for all other extension names of this builds)
        // TODO (DONAT) I think it is okay for now to apply the Java plugin and I would remove your TODO below and the 'quick solution' comment
        // Quick solution to make the 'jar' task available
        // TODO: remove this and implement a custom jar task
        project.plugins.apply('java') // TODO (DONAT) use signature that takes a class (JavaPlugin), do it like this everywhere

        // TODO (DONAT) add more assertions (feature.properties, feature.xml, license.html, epl-v10.html)
        // make sure the descriptors exist
        assert project.file('build.properties').exists()
        assert project.file('META-INF/MANIFEST.MF').exists()

        // sync jar content with the build.properties file
        PluginUtils.syncJarContentWithBuildProperties(project)

        // assemble task does't change anything outside the buildDir folder // TODO (DONAT) fix typo
        project.tasks.assemble.outputs.dir project.buildDir  // TODO (DONAT) I would remove this line, seems more like a bug in Gradle
    }

}
