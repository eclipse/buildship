/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz (vogella GmbH) - Bug 465723
 */

package org.eclipse.buildship.core.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.buildship.core.internal.configuration.ConfigurationManager;
import org.eclipse.buildship.core.internal.configuration.internal.DefaultConfigurationManager;
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.internal.console.internal.StdProcessStreamsProvider;
import org.eclipse.buildship.core.internal.event.ListenerRegistry;
import org.eclipse.buildship.core.internal.event.internal.DefaultListenerRegistry;
import org.eclipse.buildship.core.internal.invocation.InvocationCustomizer;
import org.eclipse.buildship.core.internal.launch.ExternalLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.GradleLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.impl.DefaultExternalLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.launch.impl.DefaultGradleLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.operation.ToolingApiOperationManager;
import org.eclipse.buildship.core.internal.operation.internal.DefaultToolingApiOperationManager;
import org.eclipse.buildship.core.internal.preferences.ModelPersistence;
import org.eclipse.buildship.core.internal.preferences.impl.DefaultModelPersistence;
import org.eclipse.buildship.core.internal.util.extension.InvocationCustomizerCollector;
import org.eclipse.buildship.core.internal.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.core.internal.util.logging.EclipseLogger;
import org.eclipse.buildship.core.internal.workspace.GradleWorkspaceManager;
import org.eclipse.buildship.core.internal.workspace.WorkspaceOperations;
import org.eclipse.buildship.core.internal.workspace.impl.DefaultGradleWorkspaceManager;
import org.eclipse.buildship.core.internal.workspace.impl.DefaultWorkspaceOperations;
import org.eclipse.buildship.core.internal.workspace.impl.ProjectChangeListener;
import org.eclipse.buildship.core.internal.workspace.impl.SynchronizingBuildScriptUpdateListener;

/**
 * The plug-in runtime class for the Gradle integration plugin containing the non-UI elements.
 * <p/>
 * This class is automatically instantiated by the Eclipse runtime and wired through the
 * <tt>Bundle-Activator</tt> entry in the <tt>META-INF/MANIFEST.MF</tt> file. The registered
 * instance can be obtained during runtime through the {@link CorePlugin#getInstance()} method.
 * <p/>
 * Moreover, this is the entry point for accessing associated services. All service references
 * are accessible via static methods on this class.
 * <p/>
 * The {@link #start(BundleContext)} and {@link #stop(BundleContext)} methods' responsibility is to
 * assign and free the managed services along the plugin runtime lifecycle.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class CorePlugin extends Plugin {

    public static final String PLUGIN_ID = "org.eclipse.buildship.core"; //$NON-NLS-1$

    public static final String GRADLE_JOB_FAMILY = PLUGIN_ID + ".jobs";

    private static CorePlugin plugin;

    // do not use generics-aware signature since this causes compilation troubles (JDK, Spock)
    // search the web for -target jsr14 to find out more about this obscurity
    private ServiceRegistration loggerService;
    private ServiceRegistration publishedGradleVersionsService;
    private ServiceRegistration workspaceOperationsService;
    private ServiceRegistration gradleWorkspaceManagerService;
    private ServiceRegistration processStreamsProviderService;
    private ServiceRegistration gradleLaunchConfigurationService;
    private ServiceRegistration listenerRegistryService;

    // service tracker for each service to allow to register other service implementations of the
    // same type but with higher prioritization, useful for testing
    private ServiceTracker loggerServiceTracker;
    private ServiceTracker publishedGradleVersionsServiceTracker;
    private ServiceTracker workspaceOperationsServiceTracker;
    private ServiceTracker gradleWorkspaceManagerServiceTracker;
    private ServiceTracker processStreamsProviderServiceTracker;
    private ServiceTracker gradleLaunchConfigurationServiceTracker;
    private ServiceTracker listenerRegistryServiceTracker;

    private DefaultModelPersistence modelPersistence;
    private ProjectChangeListener projectChangeListener;
    private SynchronizingBuildScriptUpdateListener buildScriptUpdateListener;
    private InvocationCustomizer invocationCustomizer;
    private ConfigurationManager configurationManager;
    private DefaultExternalLaunchConfigurationManager externalLaunchConfigurationManager;
    private ToolingApiOperationManager operationManager;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        super.start(bundleContext);
        plugin = this;
        ensureProxySettingsApplied();
        registerServices(bundleContext);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregisterServices();
        plugin = null;
        super.stop(context);
    }

    private void ensureProxySettingsApplied() throws Exception {
        // the proxy settings are set when the core.net plugin is started
        Platform.getBundle("org.eclipse.core.net").start(Bundle.START_TRANSIENT);
    }

    private void registerServices(BundleContext context) {
        // store services with low ranking such that they can be overridden
        // during testing or the like
        Dictionary<String, Object> preferences = new Hashtable<String, Object>();
        preferences.put(Constants.SERVICE_RANKING, 1);

        // initialize service trackers before the services are created
        this.loggerServiceTracker = createServiceTracker(context, Logger.class);
        this.publishedGradleVersionsServiceTracker = createServiceTracker(context, PublishedGradleVersionsWrapper.class);
        this.workspaceOperationsServiceTracker = createServiceTracker(context, WorkspaceOperations.class);
        this.gradleWorkspaceManagerServiceTracker = createServiceTracker(context, GradleWorkspaceManager.class);
        this.processStreamsProviderServiceTracker = createServiceTracker(context, ProcessStreamsProvider.class);
        this.gradleLaunchConfigurationServiceTracker = createServiceTracker(context, GradleLaunchConfigurationManager.class);
        this.listenerRegistryServiceTracker = createServiceTracker(context, ListenerRegistry.class);

        // register all services
        this.loggerService = registerService(context, Logger.class, createLogger(), preferences);
        this.publishedGradleVersionsService = registerService(context, PublishedGradleVersionsWrapper.class, createPublishedGradleVersions(), preferences);
        this.workspaceOperationsService = registerService(context, WorkspaceOperations.class, createWorkspaceOperations(), preferences);
        this.gradleWorkspaceManagerService = registerService(context, GradleWorkspaceManager.class, createGradleWorkspaceManager(), preferences);
        this.processStreamsProviderService = registerService(context, ProcessStreamsProvider.class, createProcessStreamsProvider(), preferences);
        this.gradleLaunchConfigurationService = registerService(context, GradleLaunchConfigurationManager.class, createGradleLaunchConfigurationManager(), preferences);
        this.listenerRegistryService = registerService(context, ListenerRegistry.class, createListenerRegistry(), preferences);

        this.modelPersistence = DefaultModelPersistence.createAndRegister();
        this.projectChangeListener = ProjectChangeListener.createAndRegister();
        this.buildScriptUpdateListener = SynchronizingBuildScriptUpdateListener.createAndRegister();
        this.invocationCustomizer = new InvocationCustomizerCollector();
        this.configurationManager = new DefaultConfigurationManager();
        this.externalLaunchConfigurationManager = DefaultExternalLaunchConfigurationManager.createAndRegister();
        this.operationManager = new DefaultToolingApiOperationManager();
    }

    private ServiceTracker createServiceTracker(BundleContext context, Class<?> clazz) {
        ServiceTracker serviceTracker = new ServiceTracker(context, clazz.getName(), null);
        serviceTracker.open();
        return serviceTracker;
    }

    private <T> ServiceRegistration registerService(BundleContext context, Class<T> clazz, T service, Dictionary<String, Object> properties) {
        return context.registerService(clazz.getName(), service, properties);
    }

    private EclipseLogger createLogger() {
        return new EclipseLogger(getLog(), PLUGIN_ID, isDebugging());
    }

    private PublishedGradleVersionsWrapper createPublishedGradleVersions() {
        return new PublishedGradleVersionsWrapper();
    }

    private WorkspaceOperations createWorkspaceOperations() {
        return new DefaultWorkspaceOperations();
    }

    private GradleWorkspaceManager createGradleWorkspaceManager() {
        return new DefaultGradleWorkspaceManager();
    }

    private ProcessStreamsProvider createProcessStreamsProvider() {
        return new StdProcessStreamsProvider();
    }

    private GradleLaunchConfigurationManager createGradleLaunchConfigurationManager() {
        return new DefaultGradleLaunchConfigurationManager();
    }

    private ListenerRegistry createListenerRegistry() {
        return new DefaultListenerRegistry();
    }

    private void unregisterServices() {
        this.externalLaunchConfigurationManager.unregister();
        this.buildScriptUpdateListener.close();
        this.projectChangeListener.close();
        this.modelPersistence.close();
        this.listenerRegistryService.unregister();
        this.gradleLaunchConfigurationService.unregister();
        this.processStreamsProviderService.unregister();
        this.gradleWorkspaceManagerService.unregister();
        this.workspaceOperationsService.unregister();
        this.publishedGradleVersionsService.unregister();
        this.loggerService.unregister();

        this.listenerRegistryServiceTracker.close();
        this.gradleLaunchConfigurationServiceTracker.close();
        this.processStreamsProviderServiceTracker.close();
        this.gradleWorkspaceManagerServiceTracker.close();
        this.workspaceOperationsServiceTracker.close();
        this.publishedGradleVersionsServiceTracker.close();
        this.loggerServiceTracker.close();
    }

    public static CorePlugin getInstance() {
        return plugin;
    }

    public static Logger logger() {
        return (Logger) getInstance().loggerServiceTracker.getService();
    }

    public static PublishedGradleVersionsWrapper publishedGradleVersions() {
        return (PublishedGradleVersionsWrapper) getInstance().publishedGradleVersionsServiceTracker.getService();
    }

    public static WorkspaceOperations workspaceOperations() {
        return (WorkspaceOperations) getInstance().workspaceOperationsServiceTracker.getService();
    }

    public static GradleWorkspaceManager gradleWorkspaceManager() {
        return (GradleWorkspaceManager) getInstance().gradleWorkspaceManagerServiceTracker.getService();
    }

    public static ProcessStreamsProvider processStreamsProvider() {
        return (ProcessStreamsProvider) getInstance().processStreamsProviderServiceTracker.getService();
    }

    public static GradleLaunchConfigurationManager gradleLaunchConfigurationManager() {
        return (GradleLaunchConfigurationManager) getInstance().gradleLaunchConfigurationServiceTracker.getService();
    }

    public static ListenerRegistry listenerRegistry() {
        return (ListenerRegistry) getInstance().listenerRegistryServiceTracker.getService();
    }

    public static ModelPersistence modelPersistence() {
        return getInstance().modelPersistence;
    }

    public static InvocationCustomizer invocationCustomizer() {
        return getInstance().invocationCustomizer;
    }

    public static ConfigurationManager configurationManager() {
        return getInstance().configurationManager;
    }

    public static ExternalLaunchConfigurationManager externalLaunchConfigurationManager() {
        return getInstance().externalLaunchConfigurationManager;
    }

    public static ToolingApiOperationManager operationManager() {
        return getInstance().operationManager;
    }
}
