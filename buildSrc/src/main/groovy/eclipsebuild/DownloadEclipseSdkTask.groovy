package eclipsebuild

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Files

class DownloadEclipseSdkTask extends DefaultTask {

    @Input
    String downloadUrl

    @Input
    String expectedSha256Sum

    @OutputDirectory
    File targetDir

    @TaskAction
    public void downloadSdk() {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(targetDir)
        directoryLock.lock()
        try {
            downloadEclipseSdkUnprotected(getProject())
        } finally {
            directoryLock.unlock()
        }
    }

    private void downloadEclipseSdkUnprotected(Project project) {
        // download the archive
        File sdkArchive = eclipseSdkArchive()
        project.logger.info("Download Eclipse SDK from '${downloadUrl}' to '${sdkArchive.absolutePath}'")
        project.ant.get(src: new URL(downloadUrl), dest: sdkArchive)
        verifySdk256Sum(sdkArchive)

        // extract it to the same location where it was extracted
        project.logger.info("Extract '$sdkArchive' to '$sdkArchive.parentFile.absolutePath'")
        if (OperatingSystem.current().isWindows()) {
            project.ant.unzip(src: sdkArchive, dest: sdkArchive.parentFile, overwrite: true)
        } else {
            project.ant.untar(src: sdkArchive, dest: sdkArchive.parentFile, compression: "gzip", overwrite: true)
        }

        // make it executable
        File exe = eclipseSdkExe()
        project.logger.info("Set '${exe}' executable")
        exe.setExecutable(true)
    }

    private File eclipseSdkArchive() {
        new File(targetDir, OperatingSystem.current().isWindows() ? 'eclipse-sdk.zip' : 'eclipse-sdk.tar.gz')
    }

    private File eclipseSdkExe() {
        new File(targetDir, Constants.eclipseExePath)
    }

    private void verifySdk256Sum(File sdkArchive) {
        def actualSha256Sum = calculateSdk256Sum(sdkArchive)
        if (actualSha256Sum != expectedSha256Sum) {
            throw new RuntimeException("Eclipse SDK SHA-126 sum does not match. Expected: $expectedSha256Sum, actual: $actualSha256Sum.")
        }
    }

    private String calculateSdk256Sum(File file) {
        def hashingStream = new HashingOutputStream(Hashing.sha256(), new NullOutputStream())
        Files.copy(file.toPath(), hashingStream)
        return hashingStream.hash().toString()
    }

    private class NullOutputStream extends OutputStream {
        void write(int b) { }
        void write(byte[] b) { }
        void write(byte[] b, int off, int len) { }
    }
}
