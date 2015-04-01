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
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin

/**
 * Gradle plugin for building Eclipse update sites.
 * <p>
 * An example for a valid DSL:
 * <pre>
 * apply plugin: eclipsebuild.UpdateSite
 *
 * updateSite {
 *   siteDescriptor = file("category.xml")
 *   extraResources = rootProject.files("epl-v10.html", "readme.txt")
 *   signBundles = true
 * }
 * </pre>
 * The {@code siteDescriptor} is the category definition for the P2 update site. The
 * {@code extraResoures} enumerate all extra files that should also be included in the update site.
 * The {@code signBundles} flag indicates whether the site's content should be signed.
 * <p>
 * The main tasks contributed by this plugin are responsible to generate an Eclipse Update site.
 * They are attached to the 'assemble' task. When executed, all project dependency jars are copied
 * to the build folder, signed and published to the buildDir/repository folder.
 */
class UpdateSitePlugin implements Plugin<Project> {

    // name of the root node in the DSL
    static final String DSL_EXTENSION_NAME = "updateSite"

    // task names defined in the plug-in
    static final String TASK_NAME_COPY_BUNDLES = "copyBundles"
    static final String TASK_NAME_SIGN_BUNDLES = "signBundles"
    static final String TASK_NAME_CREATE_P2_REPOSITORY = "createP2Repository"

    /**
     *  Extension class providing top-level content of the DSL definition for the plug-in.
     */
    static class Extension {
        File siteDescriptor
        FileCollection extraResources
        boolean signBundles = false
    }

    @Override
    public void apply(Project project) {
        configureProject(project)

        addTaskCopyBundles(project)
        addTaskSignBundles(project)
        addTaskCreateP2Repo(project)
    }

    static void configureProject(Project project) {
        project.plugins.apply(JavaPlugin)
        project.extensions.create(DSL_EXTENSION_NAME, Extension)
        project.updateSite.extraResources = project.files()
        project.gradle.taskGraph.whenReady {
            assert project.updateSite.siteDescriptor != null
        }
    }

    static void addTaskCopyBundles(Project project) {
        def copyPluginsAndFeaturesTask = project.task(TASK_NAME_COPY_BUNDLES) {
            group = Constants.gradleTaskGroupName
            description = "Copy bundles to the build folder before update site creation."
            def bundlesDir = new File(project.buildDir, 'unsigned-bundles')
            outputs.dir bundlesDir
            doLast { copyBundles(project, bundlesDir) }
        }
        copyPluginsAndFeaturesTask.dependsOn 'jar'
    }

    static void copyBundles(Project project, File targetDirectory) {
        // delete old content
        if (targetDirectory.exists()) {
            targetDirectory.deleteDir()
            project.logger.info("Delete '${targetDirectory.absolutePath}'")
        }

        def pluginsDir = new File(targetDirectory, 'plugins')
        def featuresDir = new File(targetDirectory, 'features')
        project.logger.info("Copy bundles and features to '${targetDirectory.absolutePath}'")

        // iterate over all the project dependencies to populate the update site with the plugins and features
        for (ProjectDependency projectDep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            def dep = projectDep.dependencyProject
            // copy the output jar for each java plugin dependency
            if (dep.plugins.hasPlugin(BundlePlugin)) {
                project.logger.debug("Copy project '${dep.name}' plugin jar '${dep.tasks.jar.outputs.files.singleFile}' to '${pluginsDir}'")
                project.copy {
                    def depJar = dep.tasks.jar.outputs.files.singleFile
                    from depJar
                    into pluginsDir
                }
            }

            // copy the output jar for each feature plugin dependency
            if (dep.plugins.hasPlugin(FeaturePlugin)) {
                project.logger.debug("Copy project '${dep.name}' feature jar '${dep.tasks.jar.outputs.files.singleFile}' to '${featuresDir}'")
                project.copy {
                    def depJar = dep.tasks.jar.outputs.files.singleFile
                    from depJar
                    into featuresDir
                }
            }
        }

        project.logger.info("Copy extra resources to '${targetDirectory.absolutePath}'")
        // copy the extra resources to the update site too
        project.copy {
            from project.updateSite.extraResources
            into targetDirectory
        }
    }


    static void addTaskSignBundles(Project project) {
        project.task(TASK_NAME_SIGN_BUNDLES, dependsOn: TASK_NAME_COPY_BUNDLES) {
            group = Constants.gradleTaskGroupName
            description = "Sign bundles before generating update site"
            // task input is the copyBundles task output
            inputs.files project.tasks.copyBundles.outputs.files
            outputs.dir new File(project.buildDir, 'signed-bundles')
            outputs.dir new File(project.buildDir, 'unsigned-bundles')
            // launch the task only if signing is enabled
            onlyIf { project.updateSite.signBundles }
            doLast {signBundles(project) }
        }
    }

    static void signBundles(Project project) {
        def unsignedRootDir = new File(project.buildDir, 'unsigned-bundles')
        def unsignedPluginsDir = new File(unsignedRootDir, 'plugins')
        def unsignedFeaturesDir = new File(unsignedRootDir, 'features')
        def signedRootDir = new File(project.buildDir, 'signed-bundles')
        def signedPluginsDir = new File(signedRootDir, 'plugins')
        def signedFeaturesDir = new File(signedRootDir, 'features')

        // clean up existing content
        signedPluginsDir.deleteDir()
        signedFeaturesDir.deleteDir()

        // recreate the folders if they didn't exist before
        signedPluginsDir.mkdirs()
        signedFeaturesDir.mkdirs()

        // closure to sign each jar
        File targetDir = signedPluginsDir
        def signBundle = {
            if (it.name.endsWith(".jar")) {
                project.logger.debug("Sign '${it.absolutePath}'")
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
            else {
                project.logger.warn("Signing should point only to jar files but found ${it.path}")
            }
        }
        project.logger.info("Create signed plugins at '${targetDir.absolutePath}'")
        unsignedPluginsDir.listFiles().each signBundle
        targetDir = signedFeaturesDir
        project.logger.info("Create signed features at '${targetDir.absolutePath}'")
        unsignedFeaturesDir.listFiles().each signBundle

        // copy the remaining resources to the signed repository
        project.logger.info("Copy remaining update site resources from '${unsignedRootDir.absolutePath}' to '${signedRootDir.absolutePath}'")
        project.copy {
            from unsignedRootDir
            include '*.*'
            into signedRootDir
        }

    }

    static void addTaskCreateP2Repo(Project project) {

        def createP2RepositoryTask = project.task(TASK_NAME_CREATE_P2_REPOSITORY, dependsOn: [
            TASK_NAME_COPY_BUNDLES,
            TASK_NAME_SIGN_BUNDLES,
            ":${BuildDefinitionPlugin.TASK_NAME_INSTALL_TARGET_PLATFORM}"
        ]) {
            group Constants.gradleTaskGroupName
            description 'Generates P2 repository from the dependent bundles and features.'
            // task input is the signBundles output
            inputs.files project.tasks.signBundles.outputs.files
            def repoDir = new File(project.buildDir, 'repository')
            outputs.dir repoDir

            doLast { createP2Repo(project, repoDir) }
        }

        project.tasks.assemble.dependsOn createP2RepositoryTask
    }


    static void createP2Repo(Project project, File repoDir) {
        // always generate a new update site
        if (repoDir.exists()) {
            project.logger.info("Delete '${repoDir.absolutePath}'")
            repoDir.deleteDir()
        }

        def unsignedRootDir = new File(project.buildDir, 'unsigned-bundles')
        def signedRootDir = new File(project.buildDir, 'signed-bundles')

        def rootDir = project.updateSite.signBundles ? signedRootDir : unsignedRootDir

        // publish artifacts in the update site
        project.logger.info("Publish plugins and features from '${rootDir.absolutePath}' to the update site '${repoDir.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher',
                    '-metadataRepository', repoDir.toURI().toURL(),
                    '-artifactRepository', repoDir.toURI().toURL(),
                    '-source', rootDir,
                    '-compress',
                    '-publishArtifacts',
                    '-configs', "ANY")
        }

        // publish the P2 category defined in the site.xml
        project.logger.info("Publish categories defined in '${project.updateSite.siteDescriptor.absolutePath}' to the update site '${repoDir.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.CategoryPublisher',
                    '-metadataRepository', repoDir.toURI().toURL(),
                    '-categoryDefinition',  project.updateSite.siteDescriptor.toURI().toURL(),
                    '-compress')
        }

        project.copy {
            from rootDir
            include '*.*'
            into repoDir
        }
    }

}
