package eclipsebuild.testing;

import org.gradle.api.tasks.testing.TestFrameworkOptions;

import java.io.File;

public class EclipseTestOptions extends TestFrameworkOptions {

    private final File projectDir;
    private final String taskPath;
    private File outputDirectory;

    public EclipseTestOptions(File projectDir, File outputDirectory, String taskPath) {
        this.projectDir = projectDir;
        this.outputDirectory = outputDirectory;
        this.taskPath = taskPath;

    }

    public File getProjectDir() {
        return projectDir;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getTaskPath() {
        return taskPath;
    }
}

