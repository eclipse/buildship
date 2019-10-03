package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

import java.io.File;
import java.io.Serializable;

public class EclipseTestSpec implements Serializable {
    private static final long serialVersionUID = 1;
    private final File projectDir;
    private final File outputDirectory;
    private final String taskPath;


    public EclipseTestSpec(EclipseTestOptions options, DefaultTestFilter filter) {
        // TODO make use of the test filtering
        this.projectDir = options.getProjectDir();
        this.outputDirectory = options.getOutputDirectory();
        this.taskPath = options.getTaskPath();
    }

    public File getProjectDir() {
        return projectDir;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getTaskPath() {
        return taskPath;
    }
}

