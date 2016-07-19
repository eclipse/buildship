/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.util.Collections;
import java.util.List;

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.workspace.GradleClasspathContainer;

/**
 * Updates the Gradle classpath container to have the correct deployment attribute if any of its
 * contents have that attribute. E.g. if dependencies inside the container are marked with
 * /WEB-INF/lib, the whole container needs to be marked with /WEB-INF/lib. This is a limitation in
 * WTP, as it does not understand/allow different deployment paths on individual dependencies.
 *
 * @author Stefan Oehme
 */
final class WtpClasspathUpdater {

    private static final String DEPLOYMENT_ATTRIBUTE = "org.eclipse.jst.component.dependency";
    private static final String NON_DEPLOYMENT_ATTRIBUTE = "org.eclipse.jst.component.nondependency";

    public static void update(IJavaProject javaProject, OmniEclipseProject project, SubMonitor progress) throws JavaModelException {
        List<OmniExternalDependency> dependencies = project.getExternalDependencies();
        String deploymentPath = getDeploymentPath(dependencies);
        if (deploymentPath != null) {
            updateDeploymentPath(javaProject, deploymentPath, progress);
        } else if (hasNonDeploymentAttributes(dependencies)) {
            markAsNonDeployed(javaProject, progress);
        }
    }

    private static String getDeploymentPath(List<OmniExternalDependency> dependencies) {
        String deploymentPath = null;
        for (OmniExternalDependency dependency : dependencies) {
            List<OmniClasspathAttribute> attributes = dependency.getClasspathAttributes().isPresent() ? dependency.getClasspathAttributes().get()
                    : Collections.<OmniClasspathAttribute>emptyList();
            for (OmniClasspathAttribute attribute : attributes) {
                if (attribute.getName().equals(DEPLOYMENT_ATTRIBUTE)) {
                    if (deploymentPath != null && !deploymentPath.equals(attribute.getValue())) {
                        throw new IllegalStateException("WTP currently does not support mixed deployment paths.");
                    }
                    deploymentPath = attribute.getValue();
                }
            }
        }
        return deploymentPath;
    }

    private static boolean hasNonDeploymentAttributes(List<OmniExternalDependency> dependencies) {
        for (OmniExternalDependency dependency : dependencies) {
            List<OmniClasspathAttribute> attributes = dependency.getClasspathAttributes().isPresent() ? dependency.getClasspathAttributes().get()
                    : Collections.<OmniClasspathAttribute>emptyList();
            for (OmniClasspathAttribute attribute : attributes) {
                if (attribute.getName().equals(NON_DEPLOYMENT_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void updateDeploymentPath(IJavaProject javaProject, String deploymentPath, SubMonitor progress) throws JavaModelException {
        GradleClasspathContainer.updateAttributes(javaProject, new IClasspathAttribute[] { JavaCore.newClasspathAttribute(DEPLOYMENT_ATTRIBUTE, deploymentPath) }, progress);
    }

    private static void markAsNonDeployed(IJavaProject javaProject, SubMonitor progress) throws JavaModelException {
        GradleClasspathContainer.updateAttributes(javaProject, new IClasspathAttribute[] { JavaCore.newClasspathAttribute(NON_DEPLOYMENT_ATTRIBUTE, "") }, progress);
    }
}
