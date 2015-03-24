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
 * Static helper functions which can be used different places around the Eclipse plugin build.
 */
public class PluginUtils {

    /**
     * Configure the project manifest file to use the META-INF/MANIFEST.MF file as a base of the output manifest instead
     * of generating one. In addition it sets the Bundle-Version attribute to the project version.
     *
     * @param project The project to configure
     */
    static void updatePluginManifest(Project project) {
        project.jar {
            manifest {
                attributes 'Bundle-Version' : project.version
                from('META-INF/MANIFEST.MF') {
                    eachEntry { entry ->
                        if (entry.key == 'Bundle-Version') {
                            entry.value = project.version
                        }
                    }
                }
            }
        }
    }

    /**
     * Parse the build.properties file from the project and set the projec output jar to have the same entries when created.
     *
     * @param project The project to configure
     */
    static void syncJarContentWithBuildProperties(Project project) {
        // load the content of the build.properties file
        def buildProperties = new Properties()
        File buildPropertiesFile = project.file('build.properties')
        def fos = new FileInputStream(buildPropertiesFile)
        buildProperties.load(fos)
        fos.close()


        // parse the content
        Set effectiveResources = new LinkedHashSet()
        if (buildProperties) {
            def virtualResources = ['.']
            buildProperties.'bin.includes'?.split(',').each { relPath ->
                if(!(relPath in virtualResources)) {
                    effectiveResources.add(relPath)
                }
            }
        }

        // configure the content of the jar file based on the file content
        for (String location in effectiveResources) {
            File resource = project.file(location)
            if (resource.isDirectory()) {
                project.jar {
                    from(location, {into(location)})
                }
            } else {
                project.jar {
                    from location
                    // modify the feature's version to the project version
                    if (resource.getName().equals("feature.xml")) {
                        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens:['0.0.1.featureversion' :'"' + project.version + '"'], beginToken: '"', endToken: '"')
                    }
                }
            }
        }
    }

}
