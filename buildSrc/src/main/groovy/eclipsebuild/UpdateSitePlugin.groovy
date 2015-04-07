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

import eclipsebuild.updatesite.CopyBundlesTask
import eclipsebuild.updatesite.CreateP2RepositoryConvention
import eclipsebuild.updatesite.CreateP2RepositoryTask
import eclipsebuild.updatesite.SignBundlesTask
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
    static final String REPOSITORY_DIR_NAME = 'repository'

    @Override
    public void apply(Project project) {
        configureProject(project)
        addTaskCopyBundles(project)
        addTaskSignBundles(project)
        addTaskCreateP2Repository(project)
        validateRequiredFilesExist(project)
        defineConventionMapping(project)
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

        def copyBundlesTask = project.task(COPY_BUNDLES_TASK_NAME, type: CopyBundlesTask, dependsOn: project.tasks.findByName('jar')) {
            group = Constants.gradleTaskGroupName
            description = 'Copies over the bundles that make up the update site.'
            targetLocation = new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
        }

        // add inputs for each plugin/feature project once this build script has been evaluated (before that, the dependencies are empty)
        project.afterEvaluate {
            for (ProjectDependency projectDependency : project.configurations.compile.dependencies.withType(ProjectDependency)) {
                def dependency = projectDependency.dependencyProject
                dependency.afterEvaluate {
                    // check if the dependent project is a bundle or feature, once its build script has been evaluated
                    if (dependency.plugins.hasPlugin(BundlePlugin) || dependency.plugins.hasPlugin(FeaturePlugin)) {
                        copyBundlesTask.inputs.files dependency.tasks.jar.outputs.files
                    }
                }
            }
        }
    }

    static void addTaskSignBundles(Project project) {
        project.task(SIGN_BUNDLES_TASK_NAME, dependsOn: COPY_BUNDLES_TASK_NAME, type: SignBundlesTask) {
            group = Constants.gradleTaskGroupName
            description = 'Signs the bundles that make up the update site.'
            unsignedBundlesDirectory = new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            signedBundlesDirectory = new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)
            onlyIf { project.updateSite.signBundles }
        }
    }

    static void addTaskCreateP2Repository(Project project) {
        def createP2RepositoryTask = project.task(CREATE_P2_REPOSITORY_TASK_NAME, type: CreateP2RepositoryTask, dependsOn: [
            COPY_BUNDLES_TASK_NAME,
            SIGN_BUNDLES_TASK_NAME,
            ":${BuildDefinitionPlugin.TASK_NAME_INSTALL_TARGET_PLATFORM}"
        ]) {
            group = Constants.gradleTaskGroupName
            description = 'Generates the P2 repository.'
            bundlesDirectory = project.updateSite.signBundles ? new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME) : new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            targetDirectory = new File(project.buildDir, REPOSITORY_DIR_NAME)
        }

        project.tasks.assemble.dependsOn createP2RepositoryTask
    }

    static void validateRequiredFilesExist(Project project) {
        project.gradle.taskGraph.whenReady {
            // make sure the required descriptors exist
            assert project.file(project.updateSite.siteDescriptor).exists()
            // todo (donat) check for extra resources if they exist
        }
    }

    static defineConventionMapping(Project project) {
        def convention = new CreateP2RepositoryConvention(project)
        project.convention.plugins.createp2repository = convention
        project.tasks.withType(CreateP2RepositoryTask.class).all { CreateP2RepositoryTask task ->
            task.conventionMapping.siteDescriptor = { convention.siteDescriptor }
            task.conventionMapping.extraResources = { convention.extraResources }
            task.conventionMapping.signBundles = { convention.signBundles }
        }
        project.tasks.withType(SignBundlesTask.class).all { SignBundlesTask task ->
            task.conventionMapping.signBundles = { convention.signBundles }
        }
    }

}
