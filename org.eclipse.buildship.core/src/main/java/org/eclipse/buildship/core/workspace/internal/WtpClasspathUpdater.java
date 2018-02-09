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
import java.util.ListIterator;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.UnsupportedConfigurationException;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;
import org.eclipse.buildship.core.omnimodel.OmniExternalDependency;
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
                        throw new UnsupportedConfigurationException("WTP currently does not support mixed deployment paths.");
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
        replaceGradleClasspathContainerAttribute(javaProject, DEPLOYMENT_ATTRIBUTE, deploymentPath, NON_DEPLOYMENT_ATTRIBUTE, progress);
    }

    private static void markAsNonDeployed(IJavaProject javaProject, SubMonitor progress) throws JavaModelException {
        replaceGradleClasspathContainerAttribute(javaProject, NON_DEPLOYMENT_ATTRIBUTE, "", DEPLOYMENT_ATTRIBUTE, progress);
    }

    private static void replaceGradleClasspathContainerAttribute(IJavaProject project, String plusKey, String plusValue, String minusKey, SubMonitor progress) throws JavaModelException {
        IClasspathEntry[] oldClasspath = project.getRawClasspath();
        IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length];
        for (int i = 0; i < oldClasspath.length; i++) {
            IClasspathEntry entry = oldClasspath[i];
            if (isGradleClasspathContainer(entry)) {
                IClasspathAttribute[] attributes = replaceClasspathAttribute(entry.getExtraAttributes(), plusKey, plusValue, minusKey);
                newClasspath[i] = JavaCore.newContainerEntry(entry.getPath(), entry.getAccessRules(), attributes, entry.isExported());
            } else {
                newClasspath[i] = entry;
            }
        }
        project.setRawClasspath(newClasspath, progress);
    }

    private static boolean isGradleClasspathContainer(IClasspathEntry entry) {
        return entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(GradleClasspathContainer.CONTAINER_PATH);
    }

    private static IClasspathAttribute[] replaceClasspathAttribute(IClasspathAttribute[] attributes, String plusKey, String plusValue, String minusKey) {
        List<IClasspathAttribute> attributesList = Lists.newArrayList(attributes);
        ListIterator<IClasspathAttribute> iterator = attributesList.listIterator();
        boolean plusPresent = false;
        while(iterator.hasNext()) {
            IClasspathAttribute attribute = iterator.next();
            if (attribute.getName().equals(minusKey)) {
                iterator.remove();
            } else if (attribute.getName().equals(plusKey)) {
                plusPresent = true;
                if (!attribute.getValue().equals(plusValue)) {
                    iterator.set(JavaCore.newClasspathAttribute(plusKey, plusValue));
                }
            }
        }

        if (!plusPresent) {
            attributesList.add(JavaCore.newClasspathAttribute(plusKey, plusValue));
        }

        return attributesList.toArray(new IClasspathAttribute[attributesList.size()]);
    }
}
