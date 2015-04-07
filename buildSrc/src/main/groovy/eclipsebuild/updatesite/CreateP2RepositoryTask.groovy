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

import eclipsebuild.Config
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


/**
 * Custom Gradle task definition to create a P2 update site.
 */
class CreateP2RepositoryTask extends ConventionTask {

    // in order to use convention mapping the Java-style getPropertyName() field accessor
    // has to be used otherwise the value will be null

    @InputDirectory
    File bundlesDirectory

    @InputFile
    File siteDescriptor

    @InputFiles
    FileCollection extraResources

    @Input
    Boolean signBundles

    @OutputDirectory
    File targetDirectory

    @TaskAction
    def createP2Repo() {

        // delete old content
        if (targetDirectory.exists()) {
            project.logger.info("Delete P2 repository directory '${targetDirectory.absolutePath}'")
            targetDirectory.deleteDir()
        }

        // publish features/plugins to the update site
        project.logger.info("Publish plugins and features from '${bundlesDirectory.absolutePath}' to the update site '${targetDirectory.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher',
                    '-metadataRepository', targetDirectory.toURI().toURL(),
                    '-artifactRepository', targetDirectory.toURI().toURL(),
                    '-source', bundlesDirectory,
                    '-compress',
                    '-publishArtifacts',
                    '-configs', 'ANY')
        }

        // publish P2 category defined in the category.xml to the update site
        project.logger.info("Publish categories defined in '${getSiteDescriptor().absolutePath}' to the update site '${targetDirectory.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.CategoryPublisher',
                    '-metadataRepository', targetDirectory.toURI().toURL(),
                    '-categoryDefinition', getSiteDescriptor().toURI().toURL(),
                    '-compress')
        }

        // copy the extra resources to the update site
        project.copy {
            from getExtraResources()
            into targetDirectory
        }
    }

}
