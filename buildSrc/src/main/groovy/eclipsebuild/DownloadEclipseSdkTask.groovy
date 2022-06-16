package eclipsebuild

import com.google.common.hash.Hashing
import com.google.common.hash.HashingOutputStream
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

import javax.inject.Inject
import java.nio.file.Files

abstract class DownloadEclipseSdkTask extends DefaultTask {

    @Input
    abstract Property<String> getDownloadUrl()

    @Input
    abstract Property<String> getExpectedSha256Sum()

    @OutputDirectory
    abstract DirectoryProperty getTargetDir()

    private final Config config

    @Inject
    DownloadEclipseSdkTask(Config config) {
        this.config = config
        targetDir.set(config.eclipseSdkDir)
    }

    @TaskAction
    void downloadSdk() {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(targetDir.get().asFile)
        directoryLock.lock()
        try {
            downloadEclipseSdkUnprotected(getProject())
        } finally {
            directoryLock.unlock()
        }
    }

    private void downloadEclipseSdkUnprotected(Project project) {
        // download the archive
        File destination = new File(targetDir.get().asFile, 'eclipse-sdk.archive')
        project.logger.info("Download Eclipse SDK from '${downloadUrl.get()}' to '$destination.absolutePath'")
        project.ant.get(src: new URL(downloadUrl.get()), dest: destination)
        verifySdk256Sum(destination)

        // extract it to the same location where it was extracted
        project.logger.info("Extract 'archive to '${targetDir.get().asFile.absolutePath}'")
        if (OperatingSystem.current().isWindows()) {
            project.ant.unzip(src: destination, dest: targetDir.get().asFile, overwrite: true)
        } else if (OperatingSystem.current().isMacOsX()) {
            try {
                project.exec { commandLine 'hdiutil', 'attach', destination.absolutePath }
                FileUtils.copyDirectory(new File('/Volumes/Eclipse/Eclipse.app'), new File(targetDir.get().asFile, 'Eclipse.app'))
            } finally {
                project.exec { commandLine 'hdiutil', 'detach', '/Volumes/Eclipse' }
            }
        } else {
            project.ant.untar(src: destination, dest: targetDir.get().asFile, compression: "gzip", overwrite: true)
        }

        // make it executable
        File exe = config.eclipseSdkExe
        project.logger.info("Set '${exe}' executable")
        exe.setExecutable(true)

        // delete archive
        if (!destination.delete()) {
            throw new RuntimeException("Cannot remove $destination.absolutePath.")
        }
    }

    private void verifySdk256Sum(File destination) {
        def actualSha256Sum = calculateSdk256Sum(destination)
        if (actualSha256Sum != expectedSha256Sum.get()) {
            throw new RuntimeException("Eclipse SDK SHA-126 sum does not match. Expected: $expectedSha256Sum.get(), actual: $actualSha256Sum.")
        }
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
