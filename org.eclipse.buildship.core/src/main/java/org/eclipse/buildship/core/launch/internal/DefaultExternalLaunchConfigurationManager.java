/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.launch.ExternalLaunchConfigurationManager;

/**
 * Default implementation for {@link ExternalLaunchConfigurationManager}.
 *
 * @author Donat Csikos
 */
public final class DefaultExternalLaunchConfigurationManager implements ExternalLaunchConfigurationManager {
    private static final Set<String> SUPPORTED_LAUNCH_CONFIG_TYPES = Sets.newHashSet("org.eclipse.jdt.launching.localJavaApplication");
    private static final String ORIGINAL_CLASSPATH_PROVIDER_ATTRIBUTE = CorePlugin.PLUGIN_ID + ".originalclasspathprovider";

    @Override
    public void updateClasspathProviders(IProject project) {
        try {
            ILaunchManager configManager = DebugPlugin.getDefault().getLaunchManager();
            for (String typeId : SUPPORTED_LAUNCH_CONFIG_TYPES) {
                ILaunchConfigurationType type = configManager.getLaunchConfigurationType(typeId);
                for(ILaunchConfiguration config : configManager.getLaunchConfigurations(type)) {
                    IJavaProject javaProject = getJavaProject(config);
                    if (javaProject != null && project.equals(javaProject.getProject())) {
                        updateClasspathProvider(config);
                    }
                }

            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot update classpath provider", e);
        }
    }

    @Override
    public void updateClasspathProvider(ILaunchConfiguration configuration) {
        try {
            if (!isSupportedType(configuration)) {
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

    private boolean isSupportedType(ILaunchConfiguration configuration) throws CoreException {
        return SUPPORTED_LAUNCH_CONFIG_TYPES.contains(configuration.getType().getIdentifier());
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
}
