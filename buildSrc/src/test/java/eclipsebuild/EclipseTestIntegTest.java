package eclipsebuild;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EclipseTestIntegTest {

    @TempDir
    File testProjectDir;

    private File settingsFile;
    private File buildFile;
    private File targetFile;
    private File testBuildFile;
    private File testBuildPropertiesFile;
    private File testBuildManifestFile;
    private File testBuildTestFile;

    @BeforeEach
    public void setup() {
        this.settingsFile = new File(this.testProjectDir, "settings.gradle");
        this.buildFile = new File(this.testProjectDir, "build.gradle");
        this.targetFile = new File(this.testProjectDir, "tooling.target");
        this.testBuildFile = new File(this.testProjectDir, "pluginTest/build.gradle");
        this.testBuildPropertiesFile = new File(this.testProjectDir, "pluginTest/build.properties");
        this.testBuildManifestFile = new File(this.testProjectDir, "pluginTest/META-INF/MANIFEST.MF");
        this.testBuildTestFile = new File(this.testProjectDir, "pluginTest/src/main/java/MyTest.java");
    }

    @Test
    public void testHelloWorldTask() throws IOException {
        writeFile(this.settingsFile, "include '" + testBuildFile.getParentFile().getName() + "'");
        writeFile(this.targetFile, "" +
                "<target name=\"Test Target Platform\" sequenceNumber=\"1\">                                                                                                \n" +
                "    <locations>                                                                                                                                            \n" +
                "        <location includeAllPlatforms=\"false\" includeConfigurePhase=\"true\" includeMode=\"planner\" includeSource=\"true\" type=\"InstallableUnit\">    \n" +
                "            <unit id=\"org.junit\" version=\"4.13.0.v20200204-1500\"/>                                                                                     \n" +
                "            <unit id=\"org.eclipse.sdk.ide\" version=\"4.15.0.I20200305-0155\"/>                                                                           \n" +
                "            <repository location=\"https://builds.gradle.org:8001/eclipse/update-site/mirror/releases-rolling\"/>                                          \n" +
                "        </location>                                                                                                                                        \n" +
                "    </locations>                                                                                                                                           \n" +
                "    <targetJRE path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8\"/>                     \n" +
                "    <launcherArgs>                                                                                                                                         \n" +
                "        <vmArgs>-XX:MaxPermSize=128M</vmArgs>                                                                                                              \n" +
                "    </launcherArgs>                                                                                                                                        \n" +
                "</target>                                                                                                                                                  ");

        writeFile(this.buildFile, "                                                         \n" +
                "plugins {                                                                          \n" +
                "    id 'eclipsebuild.build-definition'                                             \n" +
                "}                                                                                  \n" +
                "eclipseBuild {                                                                     \n" +
                "    defaultEclipseVersion = '499'                                                  \n" +
                "    targetPlatform {                                                               \n" +
                "       eclipseVersion = '499'                                                      \n" +
                "       targetDefinition = file('" + targetFile.getName() + "')                     \n" +
                "   }                                                                               \n" +
                "}                                                                                  \n" +
                "allprojects {                                                                      \n" +
                "    project.version = '0.0.1'                                                      \n" +
                "    repositories {                                                                 \n" +
                "        maven {                                                                    \n" +
                "            name = 'mavenized-target-platform'                                     \n" + // TODO this should be automatically added to the project
                "            url \"${eclipsebuild.Config.on(project).mavenizedTargetPlatformDir}\"  \n" +
                "        }                                                                          \n" +
                "    }                                                                              \n" +
                "}                                                                                  \n");

        writeFile(this.testBuildFile, "                                                     \n" +
                "plugins {                                                                          \n" +
                "    id 'eclipsebuild.test-bundle'                                                  \n" +
                "}                                                                                  \n" +
                "version = '0.0.1'                                                                  \n");



        writeFile(this.testBuildPropertiesFile, "bin.includes = .");
        writeFile(this.testBuildManifestFile, "Manifest-Version: 1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Bundle-Name: pluginTest\n" +
                "Bundle-SymbolicName: pluginTest;singleton:=true\n" +
                "Bundle-Version: 0.0.1.qualifier\n" +
                "Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n" +
                "Require-Bundle: org.eclipse.core.runtime,\n" +
                " org.junit\n" +
                "Bundle-ClassPath: .\n");

        writeFile(this.testBuildTestFile, "public class MyTest { @org.junit.Test public void test() { } }");

        BuildResult result = GradleRunner.create()
            .withProjectDir(this.testProjectDir)
            .withArguments("eclipseTest", "-i", "-s", "-x", "downloadEclipseSdk", "-PeclipseTest.debug=true") // TODO remove exclude!
            .withPluginClasspath()
            .forwardOutput()
            .build();

        result.getOutput().contains("BUILD SUCCESSFUL");
    }

    private void writeFile(File destination, String content) throws IOException {
        if (!destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
