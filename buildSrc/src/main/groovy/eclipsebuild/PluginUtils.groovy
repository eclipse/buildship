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
     * Instead of generating one, this method configures the project to use the
     * META-INF/MANIFEST.MF file. In addition it sets the Bundle-Version attribute to be in sync
     * with the current build.
     *
     * @param project the project to configure
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
     * Parses the {@code build.properties} file from the project and sets the output jar to have the same
     * entries when created.
     *
     * @param project the target project to configure
     */
    static void syncJarContentWithBuildProperties(Project project) {
        // load the content of the build.properties file
        def buildProperties = loadBuildPropertiesFileToObject(project)
        // parse the content
        Set effectiveResources = parseResourcesFromBinIncludesSection(buildProperties)
        // configure the content of the jar file based on the file content
        configureJarToIncludeResources(effectiveResources, project)
    }

    private static Properties loadBuildPropertiesFileToObject(Project project) {
        def buildProperties = new Properties()
        File buildPropertiesFile = project.file('build.properties')
        def fos = new FileInputStream(buildPropertiesFile)
        buildProperties.load(fos)
        fos.close()
        return buildProperties
    }

    private static Set parseResourcesFromBinIncludesSection(Properties buildProperties) {
        Set effectiveResources = new LinkedHashSet()
        if (buildProperties) {
            def virtualResources = ['.']
            buildProperties.'bin.includes'?.split(',').each { relPath ->
                if(!(relPath in virtualResources)) {
                    effectiveResources.add(relPath)
                }
            }
        }
        return effectiveResources
    }

    private static void configureJarToIncludeResources(Set effectiveResources, Project project) {
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
