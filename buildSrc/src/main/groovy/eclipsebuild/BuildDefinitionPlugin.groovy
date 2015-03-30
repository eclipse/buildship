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
import org.gradle.internal.os.OperatingSystem

import java.nio.channels.FileLock

/**
 * Gradle plugin for the root project of the Eclipse plugin build.
 * // TODO (DONAT) what does this plugin offer in terms of functionality?
 */
class BuildDefinitionPlugin implements Plugin<Project> {

       // TODO (DONAT) class-level javadoc
        static class Extension {

        // TODO (DONAT) this seems to be a mandatory attribute? is their an annotation for that in Gradle?
        // TODO (DONAT) if not, where do you verify the value is set and set properly and a clear error message is shown if not
        String defaultEclipseVersion
        def targetPlatforms = [:]  // why not final? initialize field in constructor

        // TODO (DONAT) who calls this method? anyone?
        def targetPlatform() {
            return targetPlatforms[defaultEclipseVersion]
        }

        def targetPlatform(Closure closure) {
            def tp = new TargetPlatform()
            tp.apply(closure)
            targetPlatforms[tp.getEclipseVersion()] = tp // TODO (DONAT) since this is groovy, we might as well use the groovy convenience to get the value
        }

    }

    // TODO (DONAT) class-level javadoc
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
        def targetPlatforms = project.container(TargetPlatform) // TODO (DONAT) is this line required? the return value is never used/
        project.extensions.create("eclipseBuild", Extension) // TODO (DONAT)  extract this string to a static constant with a good name

        Config config = Config.on(project)
        installTargetPlatformTask(project, config) // TODO (DONAT) I would first call downloadEclipseSdkTask since it logically comes before installTargetPlatformTask
        downloadEclipseSdkTask(project, config)
    }

  // TODO (DONAT) rename to something like addTaskDownloadEclipseSdk
  static void downloadEclipseSdkTask(Project project, Config config) {
        project.task("downloadEclipseSdk") {  // TODO (DONAT)  extract this string to a static constant with a good name
            group = Constants.gradleTaskGroupName
          // TODO (DONAT) description is missing

            // TODO (DONAT) to weak of a check (remove it)
            outputs.upToDateWhen {
                config.eclipseSdkExe.exists()
            }

          // TODO (DONAT) define inputs as a property that points to the full path of the sdk being downloaded
          // TODO (DONAT) define outputs as an OutputFile pointing to the location where the sdk is downloaded to

          // TODO (DONAT) add a separate task for extracting the downloaded sdk
          // TODO (DONAT) add inputs as an InputFile pointing to the location where the sdk was downloaded to (i.e. the outputs of the download tasks)
          // TODO (DONAT) add outputs as the directory where the sdk is extracted to

          // TODO (DONAT) in general (for all tasks of this class and of this build, add logging so it is clear form the log what is going on (from which utl is something downloaded?, which file is extracted, etc.)
          // TODO (DONAT) when running the build from the cmd line, you should understand what is going on (without being verbose)

          // TODO (DONAT) add an explanation on what this locking is all about
            doLast {
                File dotLock = new File(config.eclipseSdkDir, ".lock")
                if (!dotLock.exists()) {
                    dotLock.getParentFile().mkdirs();
                    dotLock.createNewFile()
                }
                FileOutputStream fos = new FileOutputStream(dotLock);
                FileLock lock = fos.getChannel().lock();
                try {
                    doDownloadEclipseSdk(project, config)
                } finally {
                    lock.release();
                    fos.close();
                }
            }
        }
    }

  // TODO (DONAT) remove the 'do' prefix
  static void doDownloadEclipseSdk(Project project, Config config) {
        config.eclipseSdkDir.mkdirs() // TODO (DONAT) why is this needed if above you already create the dirs if they do not exist already
        if (Constants.getOs() == "win32") { // // TODO (DONAT) why not just OperatingSystem.current().isWindows()?
            File targetZip = new File(config.eclipseSdkDir, "eclipse-sdk.zip")
            project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: targetZip)
            project.ant.unzip(src: targetZip, dest: targetZip.parentFile)
        } else {
            File targetTar = new File(config.eclipseSdkDir, "eclipse-sdk.tar.gz")
            project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: targetTar)
            project.ant.untar(src: targetTar, dest: targetTar.parentFile, compression: "gzip")
        }
        // TODO (DONAT) make sure that untaring / unzipping happens with _replacing_ all of the existing content

        project.logger.info("Set ${config.eclipseSdkExe} executable")
        config.eclipseSdkExe.setExecutable(true)
    }

  // TODO (DONAT) rename to something like addTaskInstallTargetPlatformTask
  static void installTargetPlatformTask(Project project, Config config) {
        project.task("installTargetPlatform", dependsOn: 'downloadEclipseSdk') {  // TODO (DONAT) extract this string to a static constant with a good name
            group = Constants.gradleTaskGroupName
            description = "Installs an Eclipse SDK along with a set of additional plugins to a local Eclipse installation located in the target platform location. To modify the used Eclipse version add -Peclipse.version=[37|42|43|44] parameter"
            // TODO (DONAT) the description above does not list all eclipse version that we can handle

          // TODO (DONAT) to weak of a check (remove it)
          outputs.upToDateWhen {
                config.mavenizedTargetPlatformDir.exists() && config.mavenizedTargetPlatformDir.list().length > 0
            }

          // TODO (DONAT) have proper inputs and outputs (the pattern should be clear by now)
          // TODO (DONAT) I guess something like 'config.eclipseSdkExe.path' as input and 'config.targetPlatformDir.path' as output

            doLast {
                File dotLock = new File(config.eclipseSdkDir, ".lock")
                if (!dotLock.exists()) {
                  // TODO (DONAT) for consistency with the method above, also call dotLock.getParentFile().mkdirs();
                  dotLock.createNewFile()
                }
                FileOutputStream fos = new FileOutputStream(dotLock);
                FileLock lock = fos.getChannel().lock();
                try {
                    doInstallTargetPlatform(project, config)
                } finally {
                    lock.release();
                    fos.close();
                }
            }
        }

        // TODO (DONAT) put into its own method (addTaskUninstallTargetPlatformTask) and call from apply method
        project.task("uninstallTargetPlatform") {
            group = Constants.gradleTaskGroupName
          // TODO (DONAT) description is missing

          // TODO (DONAT) why are all target platforms deleted and not just the one currently configured? be more specific in what is deleted (which will also make the uninstall method symmetric to the 'install' method)
          // TODO (DONAT) config.targetPlatformDir.path vs. targetPlatformDirectory
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

  // TODO (DONAT) remove the 'do' prefix
    static void doInstallTargetPlatform(Project project, Config config) {
      // TODO (DONAT) remove these checks once the proper uptodate checks of the install task are in place
        if (!config.targetPlatformDir.exists() || !config.targetPlatformDir.list().length == 0) {
          // TODO (DONAT) do we have to delete the target directory first or is this already handled by the command being invoked?
            def targetPlatform = Config.on(project).targetPlatform
            project.exec {
                // TODO (DONAT) why are these 2 defined if never used
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()

                // TODO (DONAT) explain what is going on here
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
                // TODO (DONAT) why are these 2 defined if never used
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()

                // TODO (DONAT) explain what is going on here
                commandLine(config.eclipseSdkExe.path,
                        '-application', 'org.eclipse.equinox.p2.director',
                        '-repository', targetPlatform.updateSites.join(','),
                        '-installIU', targetPlatform.features.join(','),
                        '-tag', '2-Additional-Features',
                        '-destination', config.targetPlatformDir.path,
                        '-profile', 'SDKProfile',
                        '-nosplash')
            }

            // TODO (DONAT) that should be in its own task, shouldn't it?
            // TODO (DONAT) as usual, with proper input and outputs and logging
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
