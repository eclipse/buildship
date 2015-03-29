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
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Gradle plugin for building Eclipse update sites.
 * <p>
 * An example for a valid DSL:
 * <pre>
 * apply plugin: eclipsebuild.UpdateSite
 *
 * updateSite {
 *   siteDescriptor = file("category.xml")
 * }
 * </pre>
 * Where the category.xml file exists in the project.
 * <p>
 * The main tasks contributed by this plugin are responsible to generate an Eclipse Update site. They are attached to
 * the 'assemble' task. When executed, all project dependency jars are copied to the build folder, signed and published
 * to the buildDir/repository folder.
 */
class UpdateSitePlugin implements Plugin<Project> {

    static class Extension {
        File siteDescriptor
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
        addCopyBundlesTask(project)
        addSignBundlesTask(project)
        addCreateP2RepoTask(project)
    }

    void configureProject(Project project) {
        project.plugins.apply('java')
        project.extensions.create('updateSite', Extension)
    }

    void addCopyBundlesTask(Project project) {
        def copyPluginsAndFeaturesTask = project.task("copyBundles") {
            description = "Copy update site bundles to the build folder before update site creation"
            group = Constants.gradleTaskGroupName

            // the copy's input is the project dependencies' outputs
            project.afterEvaluate {
                for (tc in project.configurations.compile.dependencies.withType(ProjectDependency)*.dependencyProject.tasks) {
                    def taskHandler = tc.findByPath("jar")
                    if(taskHandler != null) inputs.files taskHandler.outputs.files
                }
            }

            // project outputs
            def bundlesDir = new File(project.buildDir, 'unsigned-bundles')
            def pluginsDir = new File(bundlesDir, 'plugins')
            def featuresDir = new File(bundlesDir, 'features')
            outputs.dir bundlesDir
            outputs.dir pluginsDir
            outputs.dir featuresDir

            doLast {
                // delete old content
                bundlesDir.deleteDir()

                // copy the license files, as required by the Legal Process of the Eclipse Foundation
                project.copy {
                   from project.rootProject.file('epl-v10.html')
                   from project.rootProject.file('notice.html')
                   into bundlesDir
                }

                // iterate over all the project dependencies to populate the update site with the plugins and features
                for (ProjectDependency projectDep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
                    def dep = projectDep.dependencyProject
                    // copy the output jar for each java plugin dependency
                    if (dep.plugins.hasPlugin(BundlePlugin)) {
                        project.logger.debug("Copy project ${dep.name} plugin jar ${dep.tasks.jar.outputs.files.singleFile} to ${pluginsDir}")
                        project.copy {
                            def depJar = dep.tasks.jar.outputs.files.singleFile
                            from depJar
                            into pluginsDir
                        }
                    }

                    // copy the output jar for each feature plugin dependency
                    if (dep.plugins.hasPlugin(FeaturePlugin)) {
                        project.logger.debug("Copy project ${dep.name} feature jar ${dep.tasks.jar.outputs.files.singleFile} to ${featuresDir}")
                        project.copy {
                            def depJar = dep.tasks.jar.outputs.files.singleFile
                            from depJar
                            into featuresDir
                        }
                    }
                }
            }
        }
        copyPluginsAndFeaturesTask.dependsOn 'jar'
    }

    void addSignBundlesTask(Project project) {
        project.task("signBundles", dependsOn: 'copyBundles') {
            group = Constants.gradleTaskGroupName
            description = "Sign bundles before generating update site"

            // task input is the copyBundles task output
            inputs.files project.tasks.copyBundles.outputs.files

            // task output is the the plugins and features directory
            def unsignedRootDir = new File(project.buildDir, 'unsigned-bundles')
            def signedRootDir = new File(project.buildDir, 'signed-bundles')
            def unsignedPluginsDir = new File(unsignedRootDir, 'plugins')
            def unsignedFeaturesDir = new File(unsignedRootDir, 'features')
            def signedPluginsDir = new File(signedRootDir, 'plugins')
            def signedFeaturesDir = new File(signedRootDir, 'features')

            outputs.dir unsignedRootDir
            outputs.dir signedPluginsDir
            outputs.dir signedFeaturesDir
            outputs.dir unsignedPluginsDir
            outputs.dir unsignedFeaturesDir

            doLast {
                signedPluginsDir.deleteDir()
                signedFeaturesDir.deleteDir()
                signedPluginsDir.mkdirs()
                signedFeaturesDir.mkdirs()

                File targetDir = signedPluginsDir
                def signBundle = {
                    if (it.name.endsWith(".jar")) {
                        ant.signjar(
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
                        logger.warn("Signing should point only to jar files but found ${it.path}")
                    }
                }

                unsignedPluginsDir.listFiles().each signBundle
                targetDir = signedFeaturesDir
                unsignedFeaturesDir.listFiles().each signBundle

                project.copy {
                  from unsignedRootDir
                  include '*.*'
                  into signedRootDir
                }
            }
        }
    }

    void addCreateP2RepoTask(Project project) {
        // task output is a repository

        def createP2RepositoryTask = project.task("createP2Repository", dependsOn: ['copyBundles', 'signBundles', ':installTargetPlatform']) {
            group Constants.gradleTaskGroupName
            description 'Generates P2 repository with selected bundles and features.'

            // task input is the signbundles output
            inputs.files project.tasks.signBundles.outputs.files

            def repoDir = new File(project.buildDir, 'repository')
            outputs.dir repoDir

            doLast {
                // always generate a new update site
                def deleted = repoDir.deleteDir()

                def unsignedRootDir = new File(project.buildDir, 'unsigned-bundles')
                def signedRootDir = new File(project.buildDir, 'signed-bundles')

                // TODO refactor config object to a parameter (like in the other plugins)
                def version = new File(Config.on(project).targetPlatformDir, "version").text
                def rootDir = version == '3.7.2' ? unsignedRootDir : signedRootDir

                // publish artifacts in the update site
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

        project.tasks.assemble.dependsOn createP2RepositoryTask
        project.tasks.assemble.outputs.dir project.buildDir
    }

}
