package eclipsebuild.testing;

import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;

public class EclipseTestSpec implements Serializable {
    private static final long serialVersionUID = 1;
    private final File projectDir;
    private final String taskPath;
    private final File outputDirectory;
    private final String mirrors;
    private final String fragmentHost;
    private final String applicationName;
    private final File optionsFile;
    private final boolean consoleLog;
    private final String projectName;
    private final File workspace;
    private final File eclipseRuntime;
    private final boolean debug;

    public EclipseTestSpec(EclipseTestOptions options, DefaultTestFilter filter) {
        // TODO make use of the test filtering
        this.projectDir = options.getProjectDir();
        this.outputDirectory = options.getOutputDirectory();
        this.taskPath = options.getTaskPath();
        this.fragmentHost = options.getFragmentHost();
        this.applicationName = options.getApplicationName();
        this.optionsFile = options.getOptionsFile();
        this.consoleLog = options.isConsoleLog();
        this.mirrors = options.getMirrors();
        this.projectName = options.getProjectName();
        this.workspace = options.getWorkspace();
        this.eclipseRuntime = options.getEclipseRuntime();
        this.debug = options.isDebug();
    }

    public File getProjectDir() {
        return projectDir;
    }

    public String getTaskPath() {
        return taskPath;
    }

    public @Nullable File getOutputDirectory() {
        return outputDirectory;
    }

    public String getMirrors() {
        return mirrors;
    }

    public String getFragmentHost() {
        return fragmentHost;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public @Nullable File getOptionsFile() {
        return optionsFile;
    }

    public boolean isConsoleLog() {
        return consoleLog;
    }

    public String getProjectName() {
        return projectName;
    }

    public File getWorkspace() {
        return workspace;
    }

    public File getEclipseRuntime() {
        return eclipseRuntime;
    }

    public boolean isDebug() {
        return debug;
    }
}

