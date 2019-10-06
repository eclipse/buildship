package eclipsebuild.testing;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.initialization.loadercache.ClassLoaderCache;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.testing.TestClassLoaderFactory;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestFramework;
import org.gradle.api.internal.tasks.testing.WorkerTestClassProcessorFactory;
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestFrameworkOptions;
import org.gradle.internal.actor.ActorFactory;
import org.gradle.internal.id.IdGenerator;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.time.Clock;
import org.gradle.process.internal.worker.DefaultWorkerProcessBuilder;
import org.gradle.process.internal.worker.WorkerProcessBuilder;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class EclipseTestFramework implements TestFramework {
    private final EclipseTestOptions options;
    private final EclipseTestFrameworkDetector detector;
    private final DefaultTestFilter filter;
    private final TestClassLoaderFactory classLoaderFactory;

    public EclipseTestFramework(final Test testTask, DefaultTestFilter filter, Instantiator instantiator, ClassLoaderCache classLoaderCache) {
        this.filter = filter;

        options = instantiator.newInstance(EclipseTestOptions.class, testTask.getProject().getProjectDir(), new File(testTask.getProject().getBuildDir(), "build-output"), testTask.getPath(), testTask);
        conventionMapOutputDirectory(options, testTask.getReports().getHtml());
        detector = new EclipseTestFrameworkDetector(new ClassFileExtractionManager(testTask.getTemporaryDirFactory()));
        classLoaderFactory = new TestClassLoaderFactory(classLoaderCache, testTask);
    }

    private static void conventionMapOutputDirectory(EclipseTestOptions options, final DirectoryReport html) {
        new DslObject(options).getConventionMapping().map("outputDirectory", new Callable<File>() {
            public File call() {
                return html.getDestination();
            }
        });
    }

    @Override
    public TestClassProcessorFactoryImpl getProcessorFactory() {
        System.err.println("getProcessorFactory");
//        verifyConfigFailurePolicy();
//        verifyPreserveOrder();
//        verifyGroupByInstances();
        EclipseTestSpec spec = new EclipseTestSpec(options, filter);
        return new TestClassProcessorFactoryImpl(this.options.getOutputDirectory(), spec);
    }

//    private void verifyConfigFailurePolicy() {
//        if (!options.getConfigFailurePolicy().equals(TestNGOptions.DEFAULT_CONFIG_FAILURE_POLICY)) {
//            verifyMethodExists("setConfigFailurePolicy", String.class,
//                    String.format("The version of TestNG used does not support setting config failure policy to '%s'.", options.getConfigFailurePolicy()));
//        }
//    }
//
//    private void verifyPreserveOrder() {
//        if (options.getPreserveOrder()) {
//            verifyMethodExists("setPreserveOrder", boolean.class, "Preserving the order of tests is not supported by this version of TestNG.");
//        }
//    }
//
//    private void verifyGroupByInstances() {
//        if (options.getGroupByInstances()) {
//            verifyMethodExists("setGroupByInstances", boolean.class, "Grouping tests by instances is not supported by this version of TestNG.");
//        }
//    }

    private void verifyMethodExists(String methodName, Class<?> parameterType, String failureMessage) {
        try {
            createTestNg().getMethod(methodName, parameterType);
        } catch (NoSuchMethodException e) {
            throw new InvalidUserDataException(failureMessage, e);
        }
    }

    private Class<?> createTestNg() {
        try {
            return classLoaderFactory.create().loadClass("org.testng.TestNG");
        } catch (ClassNotFoundException e) {
            throw new GradleException("Could not load TestNG.", e);
        }
    }

    @Override
    public Action<WorkerProcessBuilder> getWorkerConfigurationAction() {
        System.err.println("getWorkerConfigurationAction");
        return new Action<WorkerProcessBuilder>() {
            public void execute(WorkerProcessBuilder workerProcessBuilder) {
                workerProcessBuilder.sharedPackages("eclipsebuild.testing");
                List<URL> urls = new ArrayList<>(((DefaultWorkerProcessBuilder)workerProcessBuilder).getImplementationClassPath());
                urls.addAll(Arrays.asList(((URLClassLoader)TestClassProcessorFactoryImpl.class.getClassLoader()).getURLs()));
                workerProcessBuilder.setImplementationClasspath(urls);
            }
        };
    }

    @Override
    public TestFrameworkOptions getOptions() {
        return options;
    }

    @Override
    public EclipseTestFrameworkDetector getDetector() {
        return detector;
    }

    public static class TestClassProcessorFactoryImpl implements WorkerTestClassProcessorFactory, Serializable {
        private final File testReportDir;
        private final EclipseTestSpec options;

        public TestClassProcessorFactoryImpl(File testReportDir, EclipseTestSpec options) {
            this.testReportDir = testReportDir;
            this.options = options;
        }

        @Override
        public TestClassProcessor create(ServiceRegistry serviceRegistry) {
            return new EclipseTestTestClassProcessor(testReportDir, options, serviceRegistry.get(IdGenerator.class), serviceRegistry.get(Clock.class), serviceRegistry.get(ActorFactory.class));
        }
    }
}