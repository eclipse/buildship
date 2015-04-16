package eclipsebuild

import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


/**
 * Task definition to assemble an Eclipse target platform.
 */
class AssembleTargetPlatformTask extends ConventionTask {

    // in order to use convention mapping the Java-style getPropertyName() field accessor
    // has to be used otherwise the value will be null

    @Input
    String sdkVersion

    @Input
    List<String> updateSites

    @Input
    List<String> features

    @OutputDirectory
    File nonMavenizedTargetPlatformDir

    @Input
    File eclipseSdkExe

    @TaskAction
    def assembleTargetPlatform() {
        // if multiple builds start on the same machine (which can be the case on a CI server)
        // we want to prevent them assembling the same target platform at the same time
        def lock = new FileSemaphore(getNonMavenizedTargetPlatformDir())
        try {
            lock.lock()
            assembleTargetPlatformUnprotected()
        }finally  {
            lock.unlock()
        }
    }

    def assembleTargetPlatformUnprotected() {
        // delete the target platform directory to ensure that the P2 Director creates a fresh product
        if (getNonMavenizedTargetPlatformDir().exists()) {
            project.logger.info("Delete mavenized platform directory '${getNonMavenizedTargetPlatformDir()}'")
            getNonMavenizedTargetPlatformDir().deleteDir()
        }

        // invoke the P2 director application to assemble the 'org.eclipse.sdk.ide' product
        // http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fp2_director.html
        project.logger.info("Assemble org.eclipse.sdk.ide product in '${getNonMavenizedTargetPlatformDir().absolutePath}'.\n    Update sites: '${getUpdateSites()}'")
        project.exec {

            // redirect the external process output to the logging
            standardOutput = new LogOutputStream(project.logger, LogLevel.INFO)
            errorOutput = new LogOutputStream(project.logger, LogLevel.INFO)

            commandLine(eclipseSdkExe.path,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', getUpdateSites().join(','),
                    '-installIU', 'org.eclipse.sdk.ide/' + getSdkVersion(),
                    '-tag', '1-Initial-State',
                    '-destination', getNonMavenizedTargetPlatformDir().path,
                    '-profile', 'SDKProfile',
                    '-bundlepool', getNonMavenizedTargetPlatformDir().path,
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
        project.logger.info("Installing the extra features into the sdk. \nFatures: '${getFeatures()}'.\n    Update sites: '${getUpdateSites()}'")
        project.exec {
            // redirect the external process output to the logging
            standardOutput = new LogOutputStream(project.logger, LogLevel.INFO)
            errorOutput = new LogOutputStream(project.logger, LogLevel.INFO)

            commandLine(getEclipseSdkExe().path,
                    '-application', 'org.eclipse.equinox.p2.director',
                    '-repository', getUpdateSites().join(','),
                    '-installIU', getFeatures().join(','),
                    '-tag', '2-Additional-Features',
                    '-destination', getNonMavenizedTargetPlatformDir().path,
                    '-profile', 'SDKProfile',
                    '-nosplash')
        }
    }
}
