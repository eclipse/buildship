/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild.testing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import eclipsebuild.Config;
import eclipsebuild.Constants;
import eclipsebuild.TestBundlePlugin;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.tasks.testing.*;
import org.gradle.api.internal.tasks.testing.detection.TestFrameworkDetector;
import org.gradle.api.internal.tasks.testing.processors.TestMainAction;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.initialization.DefaultBuildCancellationToken;
import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.time.Time;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.DefaultJavaExecAction;
import org.gradle.process.internal.JavaExecAction;

import org.eclipse.jdt.internal.junit.model.ITestRunListener2;
import org.eclipse.jdt.internal.junit.model.RemoteTestRunnerClient;

public final class EclipseTestExecuter implements TestExecuter<TestExecutionSpec> {

    private static final Logger LOGGER = Logging.getLogger(EclipseTestExecuter.class);

    private final Project project;
    private final Config config;
    private final BuildOperationExecutor executor;

    public EclipseTestExecuter(Project project, Config config, BuildOperationExecutor executor) {
        this.project = project;
        this.config = config;
        this.executor = executor;
    }

    @Override
    public void execute(TestExecutionSpec test, TestResultProcessor testResultProcessor) {
        LOGGER.info("Executing tests in Eclipse");

        int pdeTestPort = new PDETestPortLocator().locatePDETestPortNumber();
        if (pdeTestPort == -1) {
            throw new GradleException("Cannot allocate port for PDE test run");
        }
        LOGGER.info("Will use port {} to communicate with Eclipse.", pdeTestPort);

        runPDETestsInEclipse(test, testResultProcessor, pdeTestPort);
    }

    private EclipseTestExtension getExtension(Test testTask) {
        return (EclipseTestExtension) testTask.getProject().getExtensions().findByName("eclipseTest");
    }

    private void runPDETestsInEclipse(final TestExecutionSpec testSpec, final TestResultProcessor testResultProcessor,
            final int pdeTestPort) {

        Test testTask = ((EclipseTestExecutionSpec)testSpec).getTestTask();

        final Object testTaskOperationId = this.executor.getCurrentOperation().getParentId();
        final Object rootTestSuiteId = testTask.getPath();

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        File runDir = new File(testTask.getProject().getBuildDir(), testTask.getName());

        File testEclipseDir = new File(this.project.property("buildDir") + "/eclipseTest/eclipse");

        // File configIniFile = getInputs().getFiles().getSingleFile();
        File configIniFile = new File(testEclipseDir, "configuration/config.ini");
        assert configIniFile.exists();

        File runPluginsDir = new File(testEclipseDir, "plugins");
        LOGGER.info("Eclipse test directory is {}", runPluginsDir.getPath());
        File equinoxLauncherFile = getEquinoxLauncherFile(testEclipseDir);
        LOGGER.info("equinox launcher file {}", equinoxLauncherFile);

        final JavaExecAction javaExecHandleBuilder = new DefaultJavaExecAction(getFileResolver(testTask), new DefaultExecutorFactory().create("Exec process"), new DefaultBuildCancellationToken());
        javaExecHandleBuilder.setClasspath(this.project.files(equinoxLauncherFile));
        javaExecHandleBuilder.setMain("org.eclipse.equinox.launcher.Main");

        String javaHome = getExtension(testTask).getTestEclipseJavaHome();
        File executable = new File(javaHome, "bin/java");
        if (executable.exists()) {
            javaExecHandleBuilder.setExecutable(executable);
        } else {
            LOGGER.warn("Java executable doesn't exist: " + executable.getAbsolutePath());
        }

        List<String> programArgs = new ArrayList<String>();

        programArgs.add("-os");
        programArgs.add(Constants.getOs());
        programArgs.add("-ws");
        programArgs.add(Constants.getWs());
        programArgs.add("-arch");
        programArgs.add(Constants.getArch());

        if (getExtension(testTask).isConsoleLog()) {
            programArgs.add("-consoleLog");
        }
        File optionsFile = getExtension(testTask).getOptionsFile();
        if (optionsFile != null) {
            programArgs.add("-debug");
            programArgs.add(optionsFile.getAbsolutePath());
        }
        programArgs.add("-version");
        programArgs.add("4");
        programArgs.add("-port");
        programArgs.add(Integer.toString(pdeTestPort));
        programArgs.add("-testLoaderClass");
        programArgs.add("org.eclipse.jdt.internal.junit4.runner.JUnit4TestLoader");
        programArgs.add("-loaderpluginname");
        programArgs.add("org.eclipse.jdt.junit4.runtime");
        programArgs.add("-classNames");
        for (String clzName : collectTestNames(testTask, testTaskOperationId, rootTestSuiteId)) {
            programArgs.add(clzName);
        }
        programArgs.add("-application");
        programArgs.add(getExtension(testTask).getApplicationName());
        programArgs.add("-product org.eclipse.platform.ide");
        // alternatively can use URI for -data and -configuration (file:///path/to/dir/)
        programArgs.add("-data");
        programArgs.add(runDir.getAbsolutePath() + File.separator + "workspace");
        programArgs.add("-configuration");
        programArgs.add(configIniFile.getParentFile().getAbsolutePath());

        programArgs.add("-testpluginname");
        String fragmentHost = getExtension(testTask).getFragmentHost();
        if (fragmentHost != null) {
            programArgs.add(fragmentHost);
        } else {
            programArgs.add(this.project.getName());
        }

        javaExecHandleBuilder.setArgs(programArgs);
        javaExecHandleBuilder.setSystemProperties(testTask.getSystemProperties());
        javaExecHandleBuilder.setEnvironment(testTask.getEnvironment());

        // TODO this should be specified when creating the task (to allow override in build script)
        List<String> jvmArgs = new ArrayList<String>();
        jvmArgs.add("-XX:MaxPermSize=256m");
        jvmArgs.add("-Xms40m");
        jvmArgs.add("-Xmx1024m");

        // Java 9 workaround from https://bugs.eclipse.org/bugs/show_bug.cgi?id=493761
        // TODO we should remove this option when it is not required by Eclipse
        if (JavaVersion.current().isJava9Compatible()) {
            jvmArgs.add("--add-modules=ALL-SYSTEM");
        }
        // uncomment to debug spawned Eclipse instance
        // jvmArgs.add("-Xdebug");
        // jvmArgs.add("-Xrunjdwp:transport=dt_socket,address=8998,server=y");

        if (Constants.getOs().equals("macosx")) {
            jvmArgs.add("-XstartOnFirstThread");
        }

        // declare mirror urls if exists
        Map<String, String> mirrorUrls = new HashMap<>();
        if (project.hasProperty("mirrors")) {
            String mirrorsString = (String) project.property("mirrors");
            String[] mirrors = mirrorsString.split(",");
            for (String mirror : mirrors) {
                if (!"".equals(mirror)) {
                    String[] nameAndUrl = mirror.split(":", 2);
                    mirrorUrls.put(nameAndUrl[0], nameAndUrl[1]);
                }
            }
        }

        for (Map.Entry<String, String> mirrorUrl : mirrorUrls.entrySet()) {
            jvmArgs.add("-Dorg.eclipse.buildship.eclipsetest.mirrors." + mirrorUrl.getKey() + "=" + mirrorUrl.getValue());
        }

        javaExecHandleBuilder.setJvmArgs(jvmArgs);
        javaExecHandleBuilder.setWorkingDir(this.project.getBuildDir());

        final CountDownLatch latch = new CountDownLatch(1);
        Future<?> eclipseJob = threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ExecResult execResult = javaExecHandleBuilder.execute();
                    execResult.assertNormalExitValue();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    latch.countDown();
                }
            }
        });
        // TODO
        final String suiteName = this.project.getName();
        Future<?> testCollectorJob = threadPool.submit(new Runnable() {
            @Override
            public void run() {
                EclipseTestListener pdeTestListener = new EclipseTestListener(testResultProcessor, suiteName, this, testTaskOperationId, rootTestSuiteId);
                new RemoteTestRunnerClient().startListening(new ITestRunListener2[] { pdeTestListener }, pdeTestPort);
                LOGGER.info("Listening on port " + pdeTestPort + " for test suite " + suiteName + " results ...");
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                }
            }
        });
        try {
            latch.await(getExtension(testTask).getTestTimeoutSeconds(), TimeUnit.SECONDS);
            // short chance to do cleanup
            eclipseJob.get(15, TimeUnit.SECONDS);
            testCollectorJob.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new GradleException("Test execution failed", e);
        }
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

    private FileResolver getFileResolver(Test testTask) {
        return testTask.getProject().getPlugins().findPlugin(TestBundlePlugin.class).fileResolver;
    }

    private List<String> collectTestNames(Test testTask, Object testTaskOperationId, Object rootTestSuiteId) {
        ClassNameCollectingProcessor processor = new ClassNameCollectingProcessor();
        Runnable detector;
        final FileTree testClassFiles = testTask.getCandidateClassFiles();
        if (testTask.isScanForTestClasses()) {
            TestFrameworkDetector testFrameworkDetector = testTask.getTestFramework().getDetector();
            testFrameworkDetector.setTestClasses(testTask.getTestClassesDirs().getFiles());
            testFrameworkDetector.setTestClasspath(testTask.getClasspath().getFiles());
            detector = new EclipsePluginTestClassScanner(testClassFiles, processor);
        } else {
            detector = new EclipsePluginTestClassScanner(testClassFiles, processor);
        }

        new TestMainAction(detector, processor, new NoOpTestResultProcessor(), Time.clock(), testTaskOperationId, rootTestSuiteId, String.format("Gradle Eclipse Test Run %s", testTask.getIdentityPath())).run();
        LOGGER.info("collected test class names: {}", processor.classNames);
        return processor.classNames;
    }

    @Override
    public void stopNow() {

    }
    public static final class NoOpTestResultProcessor implements TestResultProcessor {


        @Override
        public void started(TestDescriptorInternal testDescriptorInternal, TestStartEvent testStartEvent) {
        }

        @Override
        public void completed(Object o, TestCompleteEvent testCompleteEvent) {
        }

        @Override
        public void output(Object o, TestOutputEvent testOutputEvent) {
        }

        @Override
        public void failure(Object o, Throwable throwable) {
        }
    }

    private class ClassNameCollectingProcessor implements TestClassProcessor {
        public List<String> classNames = new ArrayList<String>();

        @Override
        public void startProcessing(TestResultProcessor testResultProcessor) {
            // no-op
        }

        @Override
        public void processTestClass(TestClassRunInfo testClassRunInfo) {
            this.classNames.add(testClassRunInfo.getTestClassName());
        }

        @Override
        public void stop() {
            // no-op
        }

        @Override
        public void stopNow() {
            // no-op
        }
    }
}
