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
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.testing.Test

import eclipsebuild.testing.EclipseTestExecuter;
import eclipsebuild.testing.EclipseTestExtension;

import javax.inject.Inject

/**
 * Gradle plugin to build Eclipse test bundles.
 */
class TestBundlePlugin implements Plugin<Project> {

    public final FileResolver fileResolver

    @Inject
    public TestBundlePlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    @Override
    public void apply(Project project) {
        configureProject(project)
        createEclipseTestTask(project)
    }

    void configureProject(Project project) {
        project.extensions.create('eclipseTest', EclipseTestExtension)
        project.getPlugins().apply(eclipsebuild.BundlePlugin)
    }

    void createEclipseTestTask(Project project) {
        Config config = Config.on(project)
        def eclipseTest = project.task('eclipseTest', type: Test) {
            group = Constants.gradleTaskGroupName
            description = 'Installs all dependencies into a fresh Eclipse, runs the IDE an executes the test classes with the PDE Test Runner'

            // configure the test runner to execute all classes from the proejct
            testExecuter = new EclipseTestExecuter(project)
            testClassesDir = project.sourceSets['main'].output.classesDir
            classpath = project.sourceSets.main.output + project.sourceSets.test.output
            testSrcDirs = []
            reports.html.destination = new File("${project.reporting.baseDir}/eclipseTest")

            // set some system properties for the test Eclipse
            systemProperty('osgi.requiredJavaVersion','1.7')
            systemProperty('eclipse.pde.launch','true')
            systemProperty('eclipse.p2.data.area','@config.dir/p2')

            // The input of the task is the dependant project tasks' jar
            // the output is the folders additional plugins dir and the testing Eclipse folder
            def testDistributionDir = project.file("$project.buildDir/eclipseTest/eclipse")
            def additionalPluginsDir = project.file("$project.buildDir/eclipseTest/additions")
            outputs.dir testDistributionDir
            outputs.dir additionalPluginsDir

            // the eclipseTask input is the output jars from the dependent projects hence we have  wait until the project
            // is evaluated before we cen set the input files.
            project.afterEvaluate {
                for (tc in project.configurations.compile.dependencies.withType(ProjectDependency)*.dependencyProject.tasks) {
                    def taskHandler = tc.findByPath("jar")
                    if(taskHandler != null) inputs.files taskHandler.outputs.files
                }
            }

            doFirst {

                // Before testing, create a fresh eclipse IDE with all dependent plugins installed.
                // First delete the test eclipse distribution and the original plugins.
                testDistributionDir.deleteDir()
                additionalPluginsDir.deleteDir()

                // Copy the target platform to the test distribution folder.
                copyTargetPlatformToBuildFolder(project, config, testDistributionDir)

                // Publish the dependencies' output jars into a P2 repo in the additions folder.
                publishDependenciesIntoTemporaryRepo(project, config, additionalPluginsDir)

                // Install all elements from the generated P2 repo into the test Eclipse distributin.
                installDepedenciesIntoTargetPlatform(project, config, additionalPluginsDir, testDistributionDir)
            }
        }

        // Make sure that every time the testing is running the 'eclipseTest' task is also gets executed.
        eclipseTest.dependsOn 'test'
        eclipseTest.dependsOn 'jar'
        project.tasks.check.dependsOn eclipseTest
    }

    void copyTargetPlatformToBuildFolder(Project project, Config config,  File distro) {
        project.copy {
            from config.targetPlatformDir
            into distro
        }
    }

    void publishDependenciesIntoTemporaryRepo(Project project, Config config, File additionalPluginsDir) {
        // take all direct dependencies and and publish their jar archive to the build folder (eclipsetest/additions
        // subfolder) as a mini P2 update site

        for (ProjectDependency dep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            Project p = dep.dependencyProject
            project.exec {
                commandLine(config.eclipseSdkExe,
                        "-application", "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher",
                        "-metadataRepository", "file:${additionalPluginsDir.path}/${p.name}",
                        "-artifactRepository", "file:${additionalPluginsDir.path}/${p.name}",
                        "-bundles", p.tasks.jar.outputs.files.singleFile.path,
                        "-publishArtifacts",
                        "-nosplash")
            }
        }

        // and do the same with the current plugin
        project.exec {
            commandLine(config.eclipseSdkExe,
                    "-application", "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher",
                    "-metadataRepository", "file:${additionalPluginsDir.path}/${project.name}",
                    "-artifactRepository", "file:${additionalPluginsDir.path}/${project.name}",
                    "-bundles", project.jar.outputs.files.singleFile.path,
                    "-publishArtifacts",
                    "-nosplash")
        }
    }

    void installDepedenciesIntoTargetPlatform(Project project, Config config, File additionalPluginsDir, File testDistributionDir) {
        // take the mini P2 update sites from the build folder and install their content into the test Eclipse distro
        for (ProjectDependency dep : project.configurations.compile.dependencies.withType(ProjectDependency)) {
            Project p = dep.dependencyProject
            project.exec {
                commandLine(config.eclipseSdkExe,
                        '-application', 'org.eclipse.equinox.p2.director',
                        '-repository', "file:${additionalPluginsDir.path}/${p.name}",
                        '-installIU', p.name,
                        '-destination', testDistributionDir,
                        '-profile', 'SDKProfile',
                        '-p2.os', Constants.os,
                        '-p2.ws', Constants.ws,
                        '-p2.arch', Constants.arch,
                        '-roaming',
                        '-nosplash')
            }
        }

        // do the same with the current project
        project.exec {
            commandLine(config.eclipseSdkExe,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', "file:${additionalPluginsDir.path}/${project.name}",
                    '-installIU', project.name,
                    '-destination', testDistributionDir,
                    '-profile', 'SDKProfile',
                    '-p2.os', Constants.os,
                    '-p2.ws', Constants.ws,
                    '-p2.arch', Constants.arch,
                    '-roaming',
                    '-nosplash')
        }
    }
}
