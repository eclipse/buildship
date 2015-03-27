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
import org.gradle.api.logging.LogLevel;

import eclipsebuild.mavenize.BundleMavenDeployer

/**
 * Gradle plugin for the root project of the Eclipse plugin build.
 */
class BuildDefinitionPlugin implements Plugin<Project> {

    static class Extension {
        String defaultEclipseVersion
        def targetPlatforms = [:]

        def targetPlatform() {
            return targetPlatforms[defaultEclipseVersion]
        }

        def targetPlatform(Closure closure) {
            def tp = new TargetPlatform()
            tp.apply(closure)
            targetPlatforms[tp.getEclipseVersion()] = tp
        }
    }

    static class TargetPlatform {
        def eclipseVersion
        def sdkVersion
        def updateSites
        def features

        def apply (Closure closure) {
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = this
            closure.call()
        }
    }


    @Override
    public void apply(Project project) {
        def targetPlatforms = project.container(TargetPlatform)
        project.extensions.create("eclipseBuild", Extension)

        Config config = Config.on(project)
        installTargetPlatformTask(project, config)
        downloadEclipseSdkTask(project, config)
    }

    void downloadEclipseSdkTask(Project project, Config config) {
        project.task("downloadEclipseSdk") {
            group = Constants.gradleTaskGroupName

            outputs.upToDateWhen {
                config.eclipseSdkExe.exists()
            }

            doLast {
                File dotLock = new File(config.eclipseSdkDir, ".lock")
                if (!dotLock.exists()) {
                    dotLock.getParentFile().mkdirs();
                    dotLock.createNewFile()
                }
                FileOutputStream fos = new FileOutputStream(dotLock);
                java.nio.channels.FileLock lock = fos.getChannel().lock();
                try {
                    doDownloadEclipseSdk(project, config)
                } finally {
                    lock.release();
                    fos.close();
                }
            }
        }
    }

    void doDownloadEclipseSdk(Project project, Config config) {
        config.eclipseSdkDir.mkdirs()
        if (Constants.getOs() == "win32") {
            File targetZip = new File(config.eclipseSdkDir, "eclipse-sdk.zip")
            project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: targetZip)
            project.ant.unzip(src: targetZip, dest: targetZip.parentFile)
        } else {
            File targetTar = new File(config.eclipseSdkDir, "eclipse-sdk.tar.gz")
            project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: targetTar)
            project.ant.untar(src: targetTar, dest: targetTar.parentFile, compression: "gzip")
        }
        project.logger.info("Set ${config.eclipseSdkExe} executable")
        config.eclipseSdkExe.setExecutable(true)
    }

    void installTargetPlatformTask(Project project, Config config) {

        project.task("installTargetPlatform", dependsOn: 'downloadEclipseSdk') {
            group = Constants.gradleTaskGroupName
            description "Installs an Eclipse SDK along with a set of additional plugins to a local Eclipse installation located in the target platform location. To modify the used Eclipse version add -Peclipse.version=[37|42|43|44] parameter"

            outputs.upToDateWhen {
                config.mavenizedTargetPlatformDir.exists() && config.mavenizedTargetPlatformDir.list().length > 0
            }

            doLast {
                File dotLock = new File(config.eclipseSdkDir, ".lock")
                if (!dotLock.exists()) {
                    dotLock.createNewFile()
                }
                FileOutputStream fos = new FileOutputStream(dotLock);
                java.nio.channels.FileLock lock = fos.getChannel().lock();
                try {
                    doInstallTargetPlatform(project, config)
                } finally {
                    lock.release();
                    fos.close();
                }
            }
        }

        project.task("uninstallTargetPlatform") {
            group = Constants.gradleTaskGroupName
            doLast {
                File targetPlatformDirectory = config.containerDir
                if(!targetPlatformDirectory.exists()){
                    logger.info("Directory '${config.containerDir}' does not exist. Nothing to uninstall.")
                    return
                }

                logger.info("Deleting target platform directory '${config.containerDir}'.")
                def success = targetPlatformDirectory.deleteDir()
                if (!success) {
                    throw new RuntimeException("Deleting target platform directory '${targetPlatformDirectory}' did not succeed.")
                }
            }
        }
    }


    void doInstallTargetPlatform(Project project, Config config) {
        if (!config.targetPlatformDir.exists() || !config.targetPlatformDir.list().length == 0) {
            def targetPlatform = Config.on(project).targetPlatform
            project.exec {
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()

                commandLine(config.eclipseSdkExe.path,
                        '-application', 'org.eclipse.equinox.p2.director',
                        '-repository', targetPlatform.updateSites.join(','),
                        '-installIU', 'org.eclipse.sdk.ide/' + targetPlatform.sdkVersion,
                        '-tag', '1-Initial-State',
                        '-destination', config.targetPlatformDir.path,
                        '-profile', 'SDKProfile',
                        '-bundlepool', config.targetPlatformDir.path,
                        '-p2.os', Constants.os,
                        '-p2.ws', Constants.ws,
                        '-p2.arch', Constants.arch,
                        '-roaming',
                        '-nosplash')
            }
            project.exec {
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()

                commandLine(config.eclipseSdkExe.path,
                        '-application', 'org.eclipse.equinox.p2.director',
                        '-repository', targetPlatform.updateSites.join(','),
                        '-installIU', targetPlatform.features.join(','),
                        '-tag', '2-Additional-Features',
                        '-destination', config.targetPlatformDir.path,
                        '-profile', 'SDKProfile',
                        '-nosplash')
            }

            // create the maven repository based on the target platform
            def deployer = new BundleMavenDeployer(project.ant, Constants.mavenEclipsePluginGroupName, project.logger);
            deployer.deploy(config.targetPlatformDir, config.mavenizedTargetPlatformDir)

            // save the version in a file under the target platform directory
            def versionFile = new File(config.targetPlatformDir, 'version')
            versionFile.createNewFile()
            versionFile.text = config.eclipseVersion
        }
    }
}