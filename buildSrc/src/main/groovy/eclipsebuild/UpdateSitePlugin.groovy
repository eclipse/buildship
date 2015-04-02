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
 * apply plugin: eclipsebuild.UpdateSitePlugin
 *
 * updateSite {
 *   siteDescriptor = file('category.xml')
 *   extraResources = files('epl-v10.html', 'readme.txt')
 *   signBundles = true
 * }
 * </pre>
 * The {@code siteDescriptor} is the category definition for the P2 update site. The
 * {@code extraResources} enumerates all extra files that should be included in the update site.
 * The {@code signBundles} flag indicates whether the site's content should be signed.
 * <p>
 * The main tasks contributed by this plugin are responsible to generate an Eclipse Update site.
 * They are attached to the 'assemble' task. When executed, all project dependency jars are copied
 * to the build folder, signed and published to the buildDir/repository folder.
 */
class UpdateSitePlugin implements Plugin<Project> {

    /**
     * Extension class to configure the UpdateSite plugin.
     */
    static class Extension {
        File siteDescriptor
        FileCollection extraResources
        boolean signBundles
    }

    // name of the root node in the DSL
    static final String DSL_EXTENSION_NAME = 'updateSite'

    // task names defined in the plug-in
    static final String COPY_BUNDLES_TASK_NAME = 'copyBundles'
    static final String SIGN_BUNDLES_TASK_NAME = 'signBundles'
    static final String CREATE_P2_REPOSITORY_TASK_NAME = 'createP2Repository'

    // temporary folder names during build
    static final String UNSIGNED_BUNDLES_DIR_NAME = 'unsigned-bundles'
    static final String SIGNED_BUNDLES_DIR_NAME = 'signed-bundles'
    static final String FEATURES_DIR_NAME = 'features'
    static final String PLUGINS_DIR_NAME = 'plugins'
    static final String REPOSITORY_DIR_NAME = 'repository'

    @Override
    public void apply(Project project) {
        configureProject(project)
        addTaskCopyBundles(project)
        addTaskSignBundles(project)
        addTaskCreateP2Repository(project)
    }

    static void configureProject(Project project) {
        // apply the Java plugin to have the life-cycle tasks
        project.plugins.apply(JavaPlugin)

        // add the 'updateSite' extension
        project.extensions.create(DSL_EXTENSION_NAME, Extension)
        project.updateSite.siteDescriptor = project.file('category.xml')
        project.updateSite.extraResources = project.files()
        project.updateSite.signBundles = false

        // validate the content
        validateRequiredFilesExist(project)
    }

    static void addTaskCopyBundles(Project project) {
        def copyBundlesTask = project.task(COPY_BUNDLES_TASK_NAME) {
            group = Constants.gradleTaskGroupName
            description = 'Copies over the bundles that make up the update site.'
            outputs.dir new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            doLast { copyBundles(project) }
        }

        // add inputs for each plugin/feature project once this build script has been evaluated (before that, the dependencies are empty)
        project.afterEvaluate {
          for (ProjectDependency projectDependency : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            def dependency = projectDependency.dependencyProject
            if (dependency.plugins.hasPlugin(BundlePlugin) || dependency.plugins.hasPlugin(FeaturePlugin)) {
                copyBundlesTask.inputs.files dependency.tasks.jar
            }
          }
        }
    }

    static void copyBundles(Project project) {
        def rootDir = new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
        def pluginsDir = new File(rootDir, PLUGINS_DIR_NAME)
        def featuresDir = new File(rootDir, FEATURES_DIR_NAME)

        // delete old content
        if (rootDir.exists()) {
            project.logger.info("Delete bundles directory '${rootDir.absolutePath}'")
            rootDir.deleteDir()
        }

        // iterate over all the project dependencies to populate the update site with the plugins and features
        project.logger.info("Copy features and plugins to bundles directory '${rootDir.absolutePath}'")
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

    static void addTaskSignBundles(Project project) {
        project.task(SIGN_BUNDLES_TASK_NAME, dependsOn: COPY_BUNDLES_TASK_NAME) {
            group = Constants.gradleTaskGroupName
            description = 'Signs the bundles that make up the update site.'
            inputs.dir new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            outputs.dir new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)
            doLast { signBundles(project) }
            onlyIf { project.updateSite.signBundles }
        }
    }

    static void signBundles(Project project) {
        def unsignedRootDir = new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
        def unsignedPluginsDir = new File(unsignedRootDir, PLUGINS_DIR_NAME)
        def unsignedFeaturesDir = new File(unsignedRootDir, FEATURES_DIR_NAME)
        def signedRootDir = new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)
        def signedPluginsDir = new File(signedRootDir, PLUGINS_DIR_NAME)
        def signedFeaturesDir = new File(signedRootDir, FEATURES_DIR_NAME)

        // delete old content
        if (signedRootDir.exists()) {
            project.logger.info("Delete signed bundles directory '${signedRootDir.absolutePath}'")
            signedRootDir.deleteDir()
        }

        // create the target folders
        signedRootDir.mkdirs()
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

    static void addTaskCreateP2Repository(Project project) {
        def createP2RepositoryTask = project.task(CREATE_P2_REPOSITORY_TASK_NAME, dependsOn: [
            COPY_BUNDLES_TASK_NAME, SIGN_BUNDLES_TASK_NAME, ":${BuildDefinitionPlugin.TASK_NAME_INSTALL_TARGET_PLATFORM}"]) {
                group = Constants.gradleTaskGroupName
                description = 'Generates the P2 repository.'
                inputs.dir project.updateSite.signBundles ? new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME) : new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
                inputs.files project.updateSite.extraResources
                outputs.dir new File(project.buildDir, REPOSITORY_DIR_NAME)
                doLast { createP2Repo(project) }
        }

        project.tasks.assemble.dependsOn createP2RepositoryTask
    }

    static void createP2Repo(Project project) {
        def repositoryDir = new File(project.buildDir, REPOSITORY_DIR_NAME)

        // delete old content
        if (repositoryDir.exists()) {
            project.logger.info("Delete P2 repository directory '${repositoryDir.absolutePath}'")
            repositoryDir.deleteDir()
        }

        def unsignedRootDir = new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
        def signedRootDir = new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)
        def rootDir = project.updateSite.signBundles ? signedRootDir : unsignedRootDir

        // publish features/plugins to the update site
        project.logger.info("Publish plugins and features from '${rootDir.absolutePath}' to the update site '${repositoryDir.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher',
                    '-metadataRepository', repositoryDir.toURI().toURL(),
                    '-artifactRepository', repositoryDir.toURI().toURL(),
                    '-source', rootDir,
                    '-compress',
                    '-publishArtifacts',
                    '-configs', 'ANY')
        }

        // publish P2 category defined in the category.xml to the update site
        project.logger.info("Publish categories defined in '${project.updateSite.siteDescriptor.absolutePath}' to the update site '${repositoryDir.absolutePath}'")
        project.exec {
            commandLine(Config.on(project).eclipseSdkExe,
                    '-nosplash',
                    '-application', 'org.eclipse.equinox.p2.publisher.CategoryPublisher',
                    '-metadataRepository', repositoryDir.toURI().toURL(),
                    '-categoryDefinition',  project.updateSite.siteDescriptor.toURI().toURL(),
                    '-compress')
        }

        // copy the extra resources to the update site
        project.copy {
            from project.updateSite.extraResources
            into repositoryDir
        }
    }

    static void validateRequiredFilesExist(Project project) {
        project.gradle.taskGraph.whenReady {
            // make sure the required descriptors exist
            assert project.file(project.updateSite.siteDescriptor).exists()
        }
    }

}
