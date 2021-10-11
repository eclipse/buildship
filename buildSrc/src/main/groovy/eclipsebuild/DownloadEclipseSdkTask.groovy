package eclipsebuild


import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Files

@CacheableTask
class DownloadEclipseSdkTask extends DefaultTask {

    @Input
    String downloadUrl

    @Input
    String expectedSha256Sum

    @OutputDirectory
    File targetDir

    DownloadEclipseSdkTask() {
        onlyIf {
            File digestFile = new File(targetDir, 'digest')
            !digestFile.exists() || digestFile.text != expectedSha256Sum
        }
    }

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
        File destination = new File(targetDir, 'eclipse-sdk.archive')
        project.logger.info("Download Eclipse SDK from '${downloadUrl}' to '$destination.absolutePath'")
        project.ant.get(src: new URL(downloadUrl), dest: destination)
        verifyAndSaveSdk256Sum(targetDir, destination)

        // extract it to the same location where it was extracted
        project.logger.info("Extract 'archive to '$targetDir.absolutePath'")
        if (OperatingSystem.current().isWindows()) {
            project.ant.unzip(src: destination, dest: targetDir, overwrite: true)
        } else {
            project.ant.untar(src: destination, dest: targetDir, compression: "gzip", overwrite: true)
        }

        // make it executable
        File exe = eclipseSdkExe()
        project.logger.info("Set '${exe}' executable")
        exe.setExecutable(true)

        // delete archive
        if (!destination.delete()) {
            throw new RuntimeException("Cannot remove $destination.absolutePath.")
        }
    }

    private void verifyAndSaveSdk256Sum(File targetDir,  File destination) {
        def actualSha256Sum = calculateSdk256Sum(destination)
        if (actualSha256Sum != expectedSha256Sum) {
            throw new RuntimeException("Eclipse SDK SHA-126 sum does not match. Expected: $expectedSha256Sum, actual: $actualSha256Sum.")
        }
        new File(targetDir, 'digest').text = actualSha256Sum
    }

    private File eclipseSdkExe() {
        new File(targetDir, Constants.eclipseExePath)
    }

    private static String calculateSdk256Sum(File destination) {
        def hashingStream = new HashingOutputStream(Hashing.sha256(), new NullOutputStream())
        Files.copy(destination.toPath(), hashingStream)
        return hashingStream.hash().toString()
    }

    private static class NullOutputStream extends OutputStream {
        void write(int b) { }
        void write(byte[] b) { }
        void write(byte[] b, int off, int len) { }
    }
}
