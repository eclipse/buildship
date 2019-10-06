package eclipsebuild.testing;

import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestFrameworkOptions;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Configuration entry for the {@code test.options} block.
 */
public class EclipseTestOptions extends TestFrameworkOptions {

    private final File projectDir;
    private final Test test;

    private File outputDirectory = null;
    private String fragmentHost = null;
    private String applicationName = "org.eclipse.pde.junit.runtime.uitestapplication";
    private File optionsFile = null;
    private boolean consoleLog = false;

    public EclipseTestOptions(File projectDir, File outputDirectory, String taskPath, Test test) {
        this.projectDir = projectDir;
        this.outputDirectory = outputDirectory;
        this.test = test;
    }

    File getProjectDir() {
        return projectDir;
    }

    String getTaskPath() {
        return test.getPath();
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void outputDirectory(@Nullable File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    String getFragmentHost() {
        return fragmentHost;
    }

    public void fragmentHost(String fragmentHost) {
        this.fragmentHost = fragmentHost;
    }

    String getApplicationName() {
        return applicationName;
    }

    public void applicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    File getOptionsFile() {
        return optionsFile;
    }

    public void optionsFile(@Nullable File optionsFile) {
        this.optionsFile = optionsFile;
    }

    boolean isConsoleLog() {
        return consoleLog;
    }

    public void consoleLog(boolean consoleLog) {
        this.consoleLog = consoleLog;
    }

    boolean isDebug() {
        return test.getDebug();
    }

    @Nullable String getMirrors() {
        return (String) test.getProject().findProperty("mirrors");
    }

    String getProjectName() {
        return test.getProject().getName();
    }

    String getTestTaskName() {
        return test.getName();
    }

    public File getWorkspace() {
        return new File(test.getProject().getBuildDir().getAbsolutePath(), test.getName() + File.separator + "workspace");
    }

    public File getEclipseRuntime() {
        return new File(test.getProject().getBuildDir(), test.getName() + File.separator + "eclipse");
    }
}

