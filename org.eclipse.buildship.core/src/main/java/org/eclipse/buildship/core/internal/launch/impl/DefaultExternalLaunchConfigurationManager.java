/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNatureConfiguredEvent;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNatureDeconfiguredEvent;
import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.launch.ExternalLaunchConfigurationManager;
import org.eclipse.buildship.core.internal.workspace.ProjectCreatedEvent;
import org.eclipse.buildship.core.internal.workspace.ProjectDeletedEvent;

/**
 * Default implementation for {@link ExternalLaunchConfigurationManager}.
 *
 * @author Donat Csikos
 */
public final class DefaultExternalLaunchConfigurationManager implements ExternalLaunchConfigurationManager {

    private static final String ORIGINAL_CLASSPATH_PROVIDER_ATTRIBUTE = CorePlugin.PLUGIN_ID + ".originalclasspathprovider";

    private final LaunchConfigurationListener launchConfigurationListener = new LaunchConfigurationListener();

    private DefaultExternalLaunchConfigurationManager() {
    }

    public static DefaultExternalLaunchConfigurationManager createAndRegister() {
        DefaultExternalLaunchConfigurationManager manager = new DefaultExternalLaunchConfigurationManager();
        DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(manager.launchConfigurationListener);
        CorePlugin.listenerRegistry().addEventListener(manager.launchConfigurationListener);
        return manager;
    }

    public void unregister() {
        CorePlugin.listenerRegistry().removeEventListener(this.launchConfigurationListener);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this.launchConfigurationListener);
    }

    @Override
    public void updateClasspathProviders(IProject project) {
        try {
            ILaunchManager configManager = DebugPlugin.getDefault().getLaunchManager();
            for (SupportedLaunchConfigType supportedType : SupportedLaunchConfigType.values()) {
                ILaunchConfigurationType type = configManager.getLaunchConfigurationType(supportedType.getId());
                for (ILaunchConfiguration config : configManager.getLaunchConfigurations(type)) {
                    if (hasProject(config, project)) {
                        updateClasspathProvider(config);
                    }
                }

            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot update classpath provider", e);
        }
    }

    private static boolean hasProject(ILaunchConfiguration configuration, IProject project) throws CoreException {
        String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
        return project.getName().equals(projectName);
    }

    @Override
    public void updateClasspathProvider(ILaunchConfiguration configuration) {
        try {
            if (!SupportedLaunchConfigType.isSupported(configuration)) {
                return;
            }

            boolean isGradleProject = isGradleProject(configuration);
            boolean hasGradleClasspathProvider = hasGradleClasspathProvider(configuration);

            if (isGradleProject && !hasGradleClasspathProvider) {
                addGradleClasspathProvider(configuration);
            } else if (!isGradleProject && hasGradleClasspathProvider) {
                removeGradleClasspathProvider(configuration);
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot update classpath provider", e);
        }
    }

    private boolean isGradleProject(ILaunchConfiguration configuration) {
        IJavaProject javaProject = getJavaProject(configuration);
        return javaProject != null && GradleProjectNature.isPresentOn(javaProject.getProject());
    }

    private IJavaProject getJavaProject(ILaunchConfiguration configuration) {
        try {
            return JavaRuntime.getJavaProject(configuration);
        } catch (CoreException e) {
            return null;
        }
    }

    private boolean hasGradleClasspathProvider(ILaunchConfiguration configuration) throws CoreException {
        return GradleClasspathProvider.ID.equals(configuration.getAttributes().get(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER));
    }

    private void addGradleClasspathProvider(ILaunchConfiguration configuration) throws CoreException {
        String originalClasspathProvider = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String) null);
        Map<String, String> plusEntries = Maps.newHashMap();
        plusEntries.put(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, GradleClasspathProvider.ID);
        if (originalClasspathProvider != null) {
            plusEntries.put(ORIGINAL_CLASSPATH_PROVIDER_ATTRIBUTE, originalClasspathProvider);
        }
        updateLaunchConfiguration(configuration, plusEntries, Collections.<String> emptySet());
    }

    private void removeGradleClasspathProvider(ILaunchConfiguration configuration) throws CoreException {
        String originalClasspathProvider = configuration.getAttribute(ORIGINAL_CLASSPATH_PROVIDER_ATTRIBUTE, (String) null);
        Map<String, String> plusEntries = Maps.newHashMap();
        Set<String> minusEntries = Sets.newHashSet();
        if (originalClasspathProvider != null) {
            plusEntries.put(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, originalClasspathProvider);
            minusEntries.add(ORIGINAL_CLASSPATH_PROVIDER_ATTRIBUTE);
        } else {
            minusEntries.add(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER);
        }
        updateLaunchConfiguration(configuration, plusEntries, minusEntries);
    }

    private void updateLaunchConfiguration(ILaunchConfiguration configuration, Map<String, String> plusEntries, Set<String> minusEntries) throws CoreException {
        if (configuration instanceof ILaunchConfigurationWorkingCopy) {
            updateLaunchConfiguration((ILaunchConfigurationWorkingCopy) configuration, plusEntries, minusEntries);
        } else {
            ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
            updateLaunchConfiguration(workingCopy, plusEntries, minusEntries);
            workingCopy.doSave();
        }
    }

    private void updateLaunchConfiguration(ILaunchConfigurationWorkingCopy configuration, Map<String, String> plusEntries, Set<String> minusEntries) {
        for (String key : plusEntries.keySet()) {
            configuration.setAttribute(key, plusEntries.get(key));
        }
        for (String key : minusEntries) {
            configuration.removeAttribute(key);
        }
    }

    /**
     * Launch listener executing classpath provider updates.
     */
    private class LaunchConfigurationListener implements ILaunchConfigurationListener, EventListener {


        // If another ILaunchConfigurationListener changes the target launch configuration then
        // a new change event is generated. That event is handled synchronously invoking this
        // listener again and causing a stack overflow error.
        // see https://github.com/eclipse/buildship/issues/617
        private final ThreadLocal<Boolean> configChangeCalled = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        @Override
        public void launchConfigurationAdded(ILaunchConfiguration configuration) {
            updateClasspathProvider(configuration);
        }

        @Override
        public void launchConfigurationChanged(ILaunchConfiguration configuration) {
            if (this.configChangeCalled.get()) {
                return;
            }

            try {
                this.configChangeCalled.set(Boolean.TRUE);
                updateClasspathProvider(configuration);
            } finally {
                this.configChangeCalled.set(Boolean.FALSE);
            }
        }

        @Override
        public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
        }

        @Override
        public void onEvent(Event event) {
            if (event instanceof GradleProjectNatureConfiguredEvent) {
                updateClasspathProviders(((GradleProjectNatureConfiguredEvent)event).getProject());
            } else if (event instanceof GradleProjectNatureDeconfiguredEvent) {
                updateClasspathProviders(((GradleProjectNatureDeconfiguredEvent)event).getProject());
            } else if (event instanceof ProjectCreatedEvent) {
                updateClasspathProviders(((ProjectCreatedEvent)event).getProject());
            } else if (event instanceof ProjectDeletedEvent) {
                updateClasspathProviders(((ProjectDeletedEvent)event).getProject());
            }
        }
    }

}
