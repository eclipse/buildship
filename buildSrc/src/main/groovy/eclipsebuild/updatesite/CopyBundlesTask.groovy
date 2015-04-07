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

package eclipsebuild.updatesite

import eclipsebuild.BundlePlugin
import eclipsebuild.FeaturePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


/**
 * Custom Gradle task to copy output files from the dependent plugins and features to a target location.
 * <p/>
 * If the dependency is a plugin, it is copied to the plugins sub-folder. If the dependency is a feature
 * definition it is copied to the features sub-folder.
 */
class CopyBundlesTask extends DefaultTask {

    @OutputDirectory
    File targetLocation

    @TaskAction
    def copyBundles() {
        def pluginsDir = UpdateSiteLayout.getPluginsFolder(targetLocation)
        def featuresDir = UpdateSiteLayout.getFeaturesFolder(targetLocation)

        // delete old content
        if (targetLocation.exists()) {
            project.logger.info("Delete bundles directory '${targetLocation.absolutePath}'")
            targetLocation.deleteDir()
        }

        // iterate over all the project dependencies to populate the update site with the plugins and features
        project.logger.info("Copy features and plugins to bundles directory '${targetLocation.absolutePath}'")
        for (ProjectDependency projectDependency : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            def dependency = projectDependency.dependencyProject

            // copy the output jar for each plugin project dependency
            if (dependency.plugins.hasPlugin(BundlePlugin)) {
                project.logger.debug("Copy plugin project '${dependency.name}' with jar '${dependency.tasks.jar.outputs.files.singleFile.absolutePath}' to '${pluginsDir}'")
                project.copy {
                    from dependency.tasks.jar.outputs.files.singleFile
                    into pluginsDir
                }
            }

            // copy the output jar for each feature project dependency
            if (dependency.plugins.hasPlugin(FeaturePlugin)) {
                project.logger.debug("Copy feature project '${dependency.name}' with jar '${dependency.tasks.jar.outputs.files.singleFile.absolutePath}' to '${pluginsDir}'")
                project.copy {
                    from dependency.tasks.jar.outputs.files.singleFile
                    into featuresDir
                }
            }
        }
    }
}
