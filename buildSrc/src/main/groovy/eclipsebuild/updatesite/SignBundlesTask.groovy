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

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


/**
 * Custom Gradle task definition to sign jar files in the plugins and in the features folder.
 */
class SignBundlesTask extends ConventionTask {

    @InputDirectory
    File unsignedBundlesDirectory

    @OutputDirectory
    File signedBundlesDirectory

    // if the signBundle configuration changes we'd like to re-run this task
    @Input
    Boolean signBundles

    @TaskAction
    def signBundles() {
        def unsignedPluginsDir = UpdateSiteLayout.getPluginsFolder(unsignedBundlesDirectory)
        def unsignedFeaturesDir = UpdateSiteLayout.getFeaturesFolder(unsignedBundlesDirectory)

        def signedPluginsDir = UpdateSiteLayout.getPluginsFolder(signedBundlesDirectory)
        def signedFeaturesDir = UpdateSiteLayout.getFeaturesFolder(signedBundlesDirectory)

        // delete old content
        if (signedBundlesDirectory.exists()) {
            project.logger.info("Delete signed bundles directory '${signedBundlesDirectory.absolutePath}'")
            signedBundlesDirectory.deleteDir()
        }

        // create the target folders
        signedPluginsDir.mkdirs()
        signedFeaturesDir.mkdirs()

        // sign each plugin and feature into target location
        File targetDir = signedPluginsDir
        def signBundle = {
            project.logger.info("Sign '${it.absolutePath}'")
            project.ant.signjar(
                    verbose: 'true',
                    destDir: targetDir,
                    alias: 'EclipsePlugins',
                    jar: it,
                    keystore: project.findProject(':').file('gradle/config/signing/DevKeystore.ks'),
                    storepass: 'tooling',
                    keypass: 'tooling',
                    sigalg: 'SHA1withDSA',
                    digestalg: 'SHA1',
                    preservelastmodified: 'true')
        }
        project.logger.info("Sign plugins into '${targetDir.absolutePath}'")
        unsignedPluginsDir.listFiles().each signBundle

        project.logger.info("Sign features into '${targetDir.absolutePath}'")
        targetDir = signedFeaturesDir
        unsignedFeaturesDir.listFiles().each signBundle
    }

}
