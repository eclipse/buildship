/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473389
 */

package org.eclipse.buildship.ui;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.console.ProcessStreamsProvider;
import org.eclipse.buildship.core.notification.UserNotification;
import org.eclipse.buildship.core.util.logging.EclipseLogger;
import org.eclipse.buildship.ui.console.ConsoleProcessStreamsProvider;
import org.eclipse.buildship.ui.launch.ConsoleShowingLaunchListener;
import org.eclipse.buildship.ui.notification.DialogUserNotification;
import org.eclipse.buildship.ui.util.predicate.Predicates;
import org.eclipse.buildship.ui.util.selection.ContextActivatingSelectionListener;
import org.eclipse.buildship.ui.util.selection.ContextActivatingWindowListener;
import org.eclipse.buildship.ui.view.execution.ExecutionShowingBuildLaunchRequestListener;
import org.eclipse.buildship.ui.wizard.project.WorkingSetsAddingProjectCreatedListener;
import org.eclipse.buildship.ui.workspace.RefreshProjectCommandExecutionListener;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The plug-in runtime class for the Gradle integration plug-in containing the UI-related elements.
 * <p>
 * This class is automatically instantiated by the Eclipse runtime and wired through the
 * <tt>Bundle-Activator</tt> entry in the <tt>META-INF/MANIFEST.MF</tt> file. The registered
 * instance can be obtained during runtime through the {@link UiPlugin#getInstance()} method.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class UiPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.eclipse.buildship.ui"; //$NON-NLS-1$

    private static UiPlugin plugin;

    // do not use generics-aware signature since this causes compilation troubles (JDK, Spock)
    // search for -target jsr14 to find out more about this obscurity
    private ServiceRegistration loggerService;
    private ServiceRegistration processStreamsProviderService;
    private ServiceRegistration dialogUserNotificationService;
    private ConsoleShowingLaunchListener consoleShowingLaunchListener;
    private ExecutionShowingBuildLaunchRequestListener executionShowingBuildLaunchRequestListener;
    private WorkingSetsAddingProjectCreatedListener workingSetsAddingProjectCreatedListener;
    private ContextActivatingSelectionListener contextActivatingSelectionListener;
    private ContextActivatingWindowListener contextActivatingWindowListener;
    private RefreshProjectCommandExecutionListener refreshCommandListener;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        registerServices(context);
        registerListeners();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregisterListeners();
        unregisterServices();
        plugin = null;
        super.stop(context);
    }

    private void registerServices(BundleContext context) {
        // store services with low ranking such that they can be overridden
        // during testing or the like
        Dictionary<String, Object> preferences = new Hashtable<String, Object>();
        preferences.put(Constants.SERVICE_RANKING, 1);

        Dictionary<String, Object> priorityPreferences = new Hashtable<String, Object>();
        priorityPreferences.put(Constants.SERVICE_RANKING, 2);

        // register all services (override the ProcessStreamsProvider registered in the core plugin)
        this.loggerService = registerService(context, Logger.class, createLogger(), preferences);
        this.processStreamsProviderService = registerService(context, ProcessStreamsProvider.class, createConsoleProcessStreamsProvider(), priorityPreferences);
        this.dialogUserNotificationService = registerService(context, UserNotification.class, createUserNotification(), priorityPreferences);
    }

    private <T> ServiceRegistration registerService(BundleContext context, Class<T> clazz, T service, Dictionary<String, Object> properties) {
        return context.registerService(clazz.getName(), service, properties);
    }

    private EclipseLogger createLogger() {
        return new EclipseLogger(getLog(), PLUGIN_ID, isDebugging());
    }

    private ProcessStreamsProvider createConsoleProcessStreamsProvider() {
        return new ConsoleProcessStreamsProvider();
    }

    private UserNotification createUserNotification() {
        return new DialogUserNotification();
    }

    private void unregisterServices() {
        this.dialogUserNotificationService.unregister();
        this.processStreamsProviderService.unregister();
        this.loggerService.unregister();
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private void registerListeners() {
        this.consoleShowingLaunchListener = new ConsoleShowingLaunchListener();
        this.consoleShowingLaunchListener.handleAlreadyRunningLaunches();
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this.consoleShowingLaunchListener);

        this.executionShowingBuildLaunchRequestListener = new ExecutionShowingBuildLaunchRequestListener();
        CorePlugin.listenerRegistry().addEventListener(this.executionShowingBuildLaunchRequestListener);

        this.workingSetsAddingProjectCreatedListener = new WorkingSetsAddingProjectCreatedListener();
        CorePlugin.listenerRegistry().addEventListener(this.workingSetsAddingProjectCreatedListener);

        this.contextActivatingSelectionListener = new ContextActivatingSelectionListener(UiPluginConstants.GRADLE_NATURE_CONTEXT_ID, Predicates.hasGradleNature(), getWorkbench());
        IWorkbenchWindow[] workbenchWindows = getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
            ISelectionService selectionService = workbenchWindow.getSelectionService();
            if (selectionService != null) {
                selectionService.addSelectionListener(this.contextActivatingSelectionListener);
            }
        }

        this.contextActivatingWindowListener = new ContextActivatingWindowListener(this.contextActivatingSelectionListener);
        getWorkbench().addWindowListener(this.contextActivatingWindowListener);

        this.refreshCommandListener = new RefreshProjectCommandExecutionListener();
        ICommandService commandService = (ICommandService) getWorkbench().getService(ICommandService.class);
        commandService.addExecutionListener(this.refreshCommandListener);
    }

    @SuppressWarnings({"cast", "RedundantCast"})
    private void unregisterListeners() {
        ICommandService commandService = (ICommandService) getWorkbench().getService(ICommandService.class);
        commandService.removeExecutionListener(this.refreshCommandListener);

        getWorkbench().removeWindowListener(this.contextActivatingWindowListener);
        IWorkbenchWindow[] workbenchWindows = getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
            ISelectionService selectionService = workbenchWindow.getSelectionService();
            if (selectionService != null) {
                selectionService.removeSelectionListener(this.contextActivatingSelectionListener);
            }
        }
        CorePlugin.listenerRegistry().removeEventListener(this.workingSetsAddingProjectCreatedListener);
        CorePlugin.listenerRegistry().removeEventListener(this.executionShowingBuildLaunchRequestListener);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this.consoleShowingLaunchListener);
    }

    public static UiPlugin getInstance() {
        return plugin;
    }

    public static Logger logger() {
        return getService(getInstance().loggerService.getReference());
    }

    private static <T> T getService(ServiceReference reference) {
        return (T) reference.getBundle().getBundleContext().getService(reference);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry imageRegistry) {
        for (PluginImages pluginImage : PluginImages.values()) {
            pluginImage.register();
        }
    }

}
