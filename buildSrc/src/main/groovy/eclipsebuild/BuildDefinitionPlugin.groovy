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
import org.gradle.api.logging.LogLevel
import org.gradle.internal.os.OperatingSystem

import eclipsebuild.mavenize.BundleMavenDeployer

/**
 * Gradle plugin for the root project of the Eclipse plugin build.
 * <p/>
 * Applying this plugin offers a DSL to specify Eclipse target platforms which will be the base
 * of the compilation of the sub-projects applying applying the following plug-ins:
 * {@link BundlePlugin}, {@link TestBundlePlugin}, {@link FeaturePlugin}, {@link UpdateSitePlugin}.
 * <p/>
 * A target platform consists of a set of Eclipse update sites and a subset of their contained
 * features. Upon each build the plug-in ensures if the specified features are downloaded and
 * converted to a Maven repository.
 * <p/>
 * A valid target platform definition DSL looks like this:
 * <pre>
 * eclipseBuild {
 *     defaultEclipseVersion = '44'
 *
 *     targetPlatform {
 *         eclipseVersion = '44'
 *         sdkVersion = "4.4.2.M20150204-1700"
 *         updateSites = [
 *            "http://download.eclipse.org/release/luna",
 *            "http://download.eclipse.org/technology/swtbot/releases/latest/"
 *         ]
 *         features = [
 *             "org.eclipse.swtbot.eclipse.feature.group",
 *             "org.eclipse.swtbot.ide.feature.group"
 *         ]
 *     }
 *
 *     targetPlatform {
 *        eclipseVersion = '43'
 *        ...
 *     }
 * }
 * </pre>
 * The result is a target platform containing the Eclipse 4.4.2 SDK and the latest SWTBot. The
 * sub-projects can reference the plugins simply by defining a dependency to the required bundle
 * just like with with other dependency management tools:
 * {@code compile "eclipse:org.eclipse.swtbot.eclipse.finder:+"}. <b>Note</b> that the Eclipse SDK
 * feature is always included in the target platform.
 * <p/>
 * If no target platform version is defined for the build then the one matches to the value of the
 * {@link defaultEclipseVersion} attribute will be selected. This can be changed by appending the
 * the {@code -Peclipse.version=[version-number]} argument to he build. In the context of the
 * example above it would be:
 * <pre>
 * gradle clean build -Peclipse.version=43
 * </pre>
 * The directory layout where the target platform and it's mavenized counterpart stored is defined
 * in the {@link Config} class. The directory containing the target platforms can be redefined with
 * the {@code -PtargetPlatformsDir=<path>} argument.
 */
class BuildDefinitionPlugin implements Plugin<Project> {

    /**
     *  Extension class providing top-level content of the DSL definition for the plug-in.
     */
    static class EclipseBuild {

        def defaultEclipseVersion
        final def targetPlatforms

        EclipseBuild() {
            targetPlatforms = [:]
        }

        def targetPlatform(Closure closure) {
            def tp = new TargetPlatform()
            tp.apply(closure)
            targetPlatforms[tp.eclipseVersion] = tp
        }
    }

    /**
     * POJO class describing one target platform. Instances are stored in the {@link EclipseBuild#targetPlatforms} map.
     */
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

    // name of the root node in the DSL
    static String DSL_EXTENSION_NAME = "eclipseBuild"

    // task names
    static final String TASK_NAME_DOWNLOAD_ECLIPSE_SDK = "downloadEclipseSdk"
    static final String TASK_NAME_SAVE_TARGET_PLATFORM_DEFINITION = "saveTargetPlatformDefinition"
    static final String TASK_NAME_ASSEMBLE_TARGET_PLATFORM = "assembleTargetPlatform"
    static final String TASK_NAME_INSTALL_TARGET_PLATFORM = "installTargetPlatform"
    static final String TASK_NAME_UNINSTALL_TARGET_PLATFORM = "uninstallTargetPlatform"
    static final String TASK_NAME_UNINSTALL_ALL_TARGET_PLATFORMS = "uninstallAllTargetPlatforms"

    @Override
    public void apply(Project project) {
        project.extensions.create(DSL_EXTENSION_NAME, EclipseBuild)

        Config config = Config.on(project)
        validateDslBeforeBuildStarts(project, config)
        addTaskDownloadEclipseSdk(project, config)
        addTaskSaveTargetPlatformDefinition(project, config)
        addTaskAssembleTargetPlatform(project, config)
        addTaskInstallTargetPlatform(project, config)
        addTaskUninstallTargetPlatform(project, config)
        addTaskUninstallAllTargetPlatforms(project, config)
    }

    static void validateDslBeforeBuildStarts(Project project, Config config) {
        // check if the build definition is valid just before the build starts
        project.gradle.taskGraph.whenReady {
            if (project.eclipseBuild.defaultEclipseVersion == null) {
                throw new RuntimeException("$DSL_EXTENSION_NAME must specify 'defaultEclipseVersion'")
            }
            else if (project.eclipseBuild.targetPlatforms[config.eclipseVersion] == null) {
                throw new RuntimeException("Target platform is not defined for selected Eclipse version '${config.eclipseVersion}'")
            }
        }
    }

    static void addTaskDownloadEclipseSdk(Project project, Config config) {
        project.task(TASK_NAME_DOWNLOAD_ECLIPSE_SDK) {
            group = Constants.gradleTaskGroupName
            description = "Downloads an Eclipse SDK to perform P2 operations with."
            outputs.file config.eclipseSdkArchive
            doLast { downloadEclipseSdk(project, config) }
        }
    }

    static void downloadEclipseSdk(Project project, Config config) {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(config.eclipseSdkDir)
        try {
            directoryLock.lock()
            downloadEclipseSdkUnprotected(project, config)
        } finally {
            directoryLock.unlock()
        }
    }

    static void downloadEclipseSdkUnprotected(Project project, Config config) {
        // download the archive
        File sdkArchive = config.eclipseSdkArchive
        project.logger.info("Download Eclipse SDK from '${Constants.eclipseSdkDownloadUrl}' to '${sdkArchive.absolutePath}'")
        project.ant.get(src: Constants.eclipseSdkDownloadUrl, dest: sdkArchive)

        // extract it to the same location where it was extracted
        project.logger.info("Extract '$sdkArchive' to '$sdkArchive.parentFile.absolutePath'")
        if (OperatingSystem.current().isWindows()) {
            project.ant.unzip(src: sdkArchive, dest: sdkArchive.parentFile, overwrite: true)
        } else {
            project.ant.untar(src: sdkArchive, dest: sdkArchive.parentFile, compression: "gzip", overwrite: true)
        }

        // make it executable
        project.logger.info("Set '${config.eclipseSdkExe}' executable")
        config.eclipseSdkExe.setExecutable(true)
    }

    static void addTaskSaveTargetPlatformDefinition(Project project, Config config) {
        project.task(TASK_NAME_SAVE_TARGET_PLATFORM_DEFINITION) {
            group = Constants.gradleTaskGroupName
            description = "Persists the active target platform information to a file."
            inputs.file project.buildFile
            project.afterEvaluate { outputs.file config.targetPlatformProperties }
            doLast { saveTargetPlatformDefinition(project, config) }
        }
    }

    static void saveTargetPlatformDefinition(Project project, Config config) {
        TargetPlatform targetPlatform = project.eclipseBuild.targetPlatforms[config.eclipseVersion]
        File propertiesFile = config.targetPlatformProperties
        if (!propertiesFile.exists()) {
            propertiesFile.getParentFile().mkdirs()
            propertiesFile.createNewFile()
        }
        project.logger.info("Save target platform properties to '${propertiesFile.absolutePath}'")
        propertiesFile.withPrintWriter { writer ->
            writer.write("eclipseVersion=${targetPlatform.eclipseVersion}\n")
            writer.write("sdkVersion=${targetPlatform.sdkVersion}\n")
            writer.write("eclipseVersion=${targetPlatform.eclipseVersion}\n")
            writer.write("updateSites=${targetPlatform.updateSites.sort { it }.join(",")}\n")
            writer.write("features=${targetPlatform.features.sort{ it }.join(",")}")
        }

        project.logger.debug("Target platform properties: ${propertiesFile.text}")
    }

    static void addTaskAssembleTargetPlatform(Project project, Config config) {
        project.task(TASK_NAME_ASSEMBLE_TARGET_PLATFORM, dependsOn: [
            TASK_NAME_DOWNLOAD_ECLIPSE_SDK,
            TASK_NAME_SAVE_TARGET_PLATFORM_DEFINITION
        ]) {
            group = Constants.gradleTaskGroupName
            description = "Assembles an Eclipse distribution based on the target platform definition."
            project.afterEvaluate { inputs.file config.targetPlatformProperties }
            project.afterEvaluate { outputs.dir config.nonMavenizedTargetPlatformDir }
            doLast { assembleTargetPlatform(project, config) }
        }
    }

    static void assembleTargetPlatform(Project project, Config config) {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them assembling the same target platform at the same time
        def lock = new FileSemaphore(config.nonMavenizedTargetPlatformDir)
        try {
            lock.lock()
            assembleTargetPlatformUnprotected(project, config)
        }finally  {
            lock.unlock()
        }
    }

    static void assembleTargetPlatformUnprotected(Project project, Config config) {
        // delete the target platform directory to ensure that the P2 Director creates a fresh product
        if (config.nonMavenizedTargetPlatformDir.exists()) {
            project.logger.info("Delete mavenized platform directory '${config.nonMavenizedTargetPlatformDir}'")
            config.nonMavenizedTargetPlatformDir.deleteDir()
        }

        // invoke the P2 director application to assemble the 'org.eclipse.sdk.ide' product
        // http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fp2_director.html
        project.logger.info("Assemble org.eclipse.sdk.ide product in '${config.nonMavenizedTargetPlatformDir.absolutePath}'.\n    Update sites: '${config.targetPlatform.updateSites.join(' ')}'")
        project.exec {

            // redirect the external process output to the logging
            standardOutput = new LogOutputStream(project.logger, LogLevel.INFO)
            errorOutput = new LogOutputStream(project.logger, LogLevel.INFO)

            commandLine(config.eclipseSdkExe.path,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', config.targetPlatform.updateSites.join(','),
                    '-installIU', 'org.eclipse.sdk.ide/' + config.targetPlatform.sdkVersion,
                    '-tag', '1-Initial-State',
                    '-destination', config.nonMavenizedTargetPlatformDir.path,
                    '-profile', 'SDKProfile',
                    '-bundlepool', config.nonMavenizedTargetPlatformDir.path,
                    '-p2.os', Constants.os,
                    '-p2.ws', Constants.ws,
                    '-p2.arch', Constants.arch,
                    '-roaming',
                    '-nosplash')
        }

        // install the features defined in the target platform into the 'org.eclipse.sdk.ide' using
        // the P2 Director application. after it is finished, the destination folder contains a
        // complete Eclipse distribution with the SDK and the required features installed and can
        // be converted into a Maven repository.
        project.logger.info("Installing the extra features into the sdk. \nFatures: '${config.targetPlatform.features.join(' ')}'.\n    Update sites: '${config.targetPlatform.updateSites.join(' ')}'")
        project.exec {
            // redirect the external process output to the logging
            standardOutput = new LogOutputStream(project.logger, LogLevel.INFO)
            errorOutput = new LogOutputStream(project.logger, LogLevel.INFO)

            commandLine(config.eclipseSdkExe.path,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', config.targetPlatform.updateSites.join(','),
                    '-installIU', config.targetPlatform.features.join(','),
                    '-tag', '2-Additional-Features',
                    '-destination', config.nonMavenizedTargetPlatformDir.path,
                    '-profile', 'SDKProfile',
                    '-nosplash')
        }
    }

    static void addTaskInstallTargetPlatform(Project project, Config config) {
        project.task(TASK_NAME_INSTALL_TARGET_PLATFORM, dependsOn: TASK_NAME_ASSEMBLE_TARGET_PLATFORM) {
            group = Constants.gradleTaskGroupName
            description = "Converts the assembled Eclipse distribution to a Maven repoository."
            project.afterEvaluate { inputs.dir config.nonMavenizedTargetPlatformDir }
            project.afterEvaluate { outputs.dir config.mavenizedTargetPlatformDir }
            doLast { installTargetPlatform(project, config) }
        }
    }

    static void installTargetPlatform(Project project, Config config) {
        // delete the mavenized target platform directory to ensure that the deployment doesn't
        // have outdated artifacts
        if (config.mavenizedTargetPlatformDir.exists()) {
            project.logger.info("Delete mavenized platform directory '${config.mavenizedTargetPlatformDir}'")
            config.mavenizedTargetPlatformDir.deleteDir()
        }

        // install bundles
        project.logger.info("Convert Eclipse target platform '${config.nonMavenizedTargetPlatformDir}' to Maven repository '${config.mavenizedTargetPlatformDir}'")
        def deployer = new BundleMavenDeployer(project.ant, Constants.mavenizedEclipsePluginGroupName, project.logger)
        deployer.deploy(config.nonMavenizedTargetPlatformDir, config.mavenizedTargetPlatformDir)
    }

    static void addTaskUninstallTargetPlatform(Project project, Config config) {
        project.task(TASK_NAME_UNINSTALL_TARGET_PLATFORM) {
            group = Constants.gradleTaskGroupName
            description = "Deletes the target platform."
            doLast { deleteFolder(project, config.targetPlatformDir) }
        }
    }

    static void deleteFolder(Project project, File folder) {
        if (!folder.exists()) {
            project.logger.info("'$folder' doesn't exist")
        }
        else {
            project.logger.info("Delete '$folder'")
            def success = folder.deleteDir()
            if (!success) {
                throw new RuntimeException("Failed to delete '$folder'")
            }
        }
    }

    static void addTaskUninstallAllTargetPlatforms(Project project, Config config) {
        project.task(TASK_NAME_UNINSTALL_ALL_TARGET_PLATFORMS) {
            group = Constants.gradleTaskGroupName
            description = "Deletes all target platforms from the current machine."
            doLast { deleteFolder(project, config.targetPlatformsDir) }
        }
    }
}
