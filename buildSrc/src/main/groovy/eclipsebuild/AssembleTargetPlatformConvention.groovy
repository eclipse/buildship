    package eclipsebuild

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;


/**
 * Fields to set for the {@link AssembleTargetPlatformTask}
 */
class AssembleTargetPlatformConvention {

    final Project project

    String sdkVersion
    String updateSites
    String features
    File nonMavenizedTargetPlatformDir
    File eclipseSdkExe

    public AssembleTargetPlatformConvention(Project project) {
        this.project = project
    }

    def getSdkVersion() {
        Config.on(project).targetPlatform.sdkVersion
    }

    def getUpdateSites() {
        Config.on(project).targetPlatform.updateSites
    }

    def getFeatures() {
        Config.on(project).targetPlatform.features
    }

    def getNonMavenizedTargetPlatformDir() {
        Config.on(project).nonMavenizedTargetPlatformDir
    }

    def getEclipseSdkExe() {
        Config.on(project).eclipseSdkExe
    }

}
