package eclipsebuild.testing;

import org.eclipse.jdt.internal.junit.model.ITestRunListener2;
import org.eclipse.jdt.internal.junit.model.RemoteTestRunnerClient;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestResultProcessor;
import org.gradle.internal.actor.Actor;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.time.Clock;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EclipseTestTestClassProcessor implements TestClassProcessor {
    private final List<String> testClassNames = new ArrayList<String>();
    private final File testReportDir;
    private final EclipseTestSpec options;
    private final IdGenerator<?> idGenerator;
    private final Clock clock;
    private final ActorFactory actorFactory;
    private Actor resultProcessorActor;
    private TestResultProcessor resultProcessor;

    // TODO (donat) handle cancellation
    // TODO (donat) handle ignoring tests

    public EclipseTestTestClassProcessor(File testReportDir, EclipseTestSpec options, IdGenerator<?> idGenerator, Clock clock, ActorFactory actorFactory) {
        this.testReportDir = testReportDir;
        this.options = options;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.actorFactory = actorFactory;
    }

    @Override
    public void startProcessing(TestResultProcessor resultProcessor) {
        // Wrap the processor in an actor, to make it thread-safe
        resultProcessorActor = actorFactory.createBlockingActor(resultProcessor);
        this.resultProcessor = resultProcessorActor.getProxy(TestResultProcessor.class);
    }

    @Override
    public void processTestClass(TestClassRunInfo testClass) {
        testClassNames.add(testClass.getTestClassName());
    }

    @Override
    public void stop() {
        try {
            runTests();
        } finally {
            resultProcessorActor.stop();
        }
    }

    @Override
    public void stopNow() {
        throw new UnsupportedOperationException("stopNow() should not be invoked on remote worker TestClassProcessor");
    }

    private void runTests() {
        final int pdeTestPort = locatePDETestPortNumber();
        if (pdeTestPort == -1) {
            throw new GradleException("Cannot allocate port for PDE test run");
        }

        File eclipseRuntime = options.getEclipseRuntime();

        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + "/bin/java");
        command.add("-cp");
        command.add(getEquinoxLauncherFile(eclipseRuntime).getAbsolutePath());

        command.add("-XX:MaxPermSize=256m");
        command.add("-Xms40m");
        command.add("-Xmx1024m");

        // Java 9 workaround from https://bugs.eclipse.org/bugs/show_bug.cgi?id=493761
        // TODO we should remove this option when it is not required by Eclipse
        if (JavaVersion.current().isJava9Compatible()) {
            command.add("--add-modules=ALL-SYSTEM");
        }
        // uncomment to debug spawned Eclipse instance
        // jvmArgs.add("-Xdebug");
        // jvmArgs.add("-Xrunjdwp:transport=dt_socket,address=8998,server=y");

        if (getOs().equals("macosx")) {
            command.add("-XstartOnFirstThread");
        }

        // declare mirror urls if exists
        Map<String, String> mirrorUrls = new HashMap<>();
        if (options.getMirrors() != null) {
            String mirrorsString = options.getMirrors();
            String[] mirrors = mirrorsString.split(",");
            for (String mirror : mirrors) {
                if (!"".equals(mirror)) {
                    String[] nameAndUrl = mirror.split(":", 2);
                    mirrorUrls.put(nameAndUrl[0], nameAndUrl[1]);
                }
            }
        }

        for (Map.Entry<String, String> mirrorUrl : mirrorUrls.entrySet()) {
            command.add("-Dorg.eclipse.buildship.eclipsetest.mirrors." + mirrorUrl.getKey() + "=" + mirrorUrl.getValue());
        }

        // Java 9 workaround from https://bugs.eclipse.org/bugs/show_bug.cgi?id=493761
        // TODO we should remove this option when it is not required by Eclipse
        if (JavaVersion.current().isJava9Compatible()) {
            command.add("--add-modules=ALL-SYSTEM");
        }
        // uncomment to debug spawned Eclipse instance
        // jvmArgs.add("-Xdebug");
        // jvmArgs.add("-Xrunjdwp:transport=dt_socket,address=8998,server=y");

        if (getOs().equals("macosx")) {
            command.add("-XstartOnFirstThread");
        }

        command.add("org.eclipse.equinox.launcher.Main");

        command.add("-os");
        command.add(getOs());
        command.add("-ws");
        command.add(getWs());
        command.add("-arch");
        command.add(getArch());

        if (options.isConsoleLog()) {
            command.add("-consoleLog");
        }
        File optionsFile = options.getOptionsFile();
        if (optionsFile != null) {
            command.add("-debug");
            command.add(optionsFile.getAbsolutePath());
        }

        command.add("-version");
        command.add("4");
        command.add("-port");
        command.add(Integer.toString(pdeTestPort));
        command.add("-testLoaderClass");
        command.add("org.eclipse.jdt.internal.junit4.runner.JUnit4TestLoader");
        command.add("-loaderpluginname");
        command.add("org.eclipse.jdt.junit4.runtime");

        command.add("-classNames");
        command.addAll(testClassNames);

        command.add("-application");
        command.add(options.getApplicationName());

        command.add("-product org.eclipse.platform.ide");
        // alternatively can use URI for -data and -configuration (file:///path/to/dir/)
        command.add("-data");
        command.add(options.getWorkspace().getAbsolutePath());
        command.add("-configuration");
        command.add(new File(eclipseRuntime, "configuration").getAbsolutePath());

        command.add("-testpluginname");
        String fragmentHost = options.getFragmentHost();
        if (fragmentHost != null) {
            command.add(fragmentHost);
        } else {
            command.add(options.getProjectName());
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(options.getProjectDir());
        pb.inheritIO();
        try {
            Process start = pb.start();
            final Object rootTestSuiteId = options.getTaskPath();
            EclipseTestAdapter testAdapter = new EclipseTestAdapter(resultProcessor, rootTestSuiteId, clock, idGenerator);
            RemoteTestRunnerClient client = new RemoteTestRunnerClient();
            client.startListening(new ITestRunListener2[] { testAdapter }, pdeTestPort);
            start.waitFor();
            client.stopWaiting();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static int locatePDETestPortNumber() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
            // ignore
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return -1;
    }

    private File getEquinoxLauncherFile(File testEclipseDir) {
        File[] plugins = new File(testEclipseDir, "plugins").listFiles();
        for (File plugin : plugins) {
            if (plugin.getName().startsWith("org.eclipse.equinox.launcher_")) {
                return plugin;
            }
        }
        return null;
    }

    static String getOs() {
        OperatingSystem os = OperatingSystem.current();
        return os.isLinux() ? "linux" : os.isWindows() ? "win32" : os.isMacOsX() ? "macosx": null;
    }

    static String getWs() {
        OperatingSystem os = OperatingSystem.current();
        return os.isLinux() ? "gtk" : os.isWindows() ? "win32" : os.isMacOsX() ? "cocoa" : null;
    }

    static String getArch() {
        return System.getProperty("os.arch").contains("64") ? "x86_64" : "x86";
    }
}

