/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.Lists;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.DefaultGradleBuild;
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils;

/**
 * Updates the Gradle classpath container to have the correct deployment attribute if any of its
 * contents have that attribute. E.g. if dependencies inside the container are marked with
 * /WEB-INF/lib, the whole container needs to be marked with /WEB-INF/lib. This is a limitation in
 * WTP, as it does not understand/allow different deployment paths on individual dependencies.
 *
 * @author Stefan Oehme
 */
public class WtpConfigurator implements ProjectConfigurator {

    private static final String DEPLOYMENT_ATTRIBUTE = "org.eclipse.jst.component.dependency";
    private static final String NON_DEPLOYMENT_ATTRIBUTE = "org.eclipse.jst.component.nondependency";

    private DefaultGradleBuild gradleBuild;
    private Map<File, EclipseProject> locationToProject;

    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
        // TODO (donat) add required model declarations to the project configurator extension point
        GradleBuild gradleBuild = context.getGradleBuild();
        try {
            Collection<EclipseProject> rootModels = gradleBuild.withConnection(connection -> ExtendedEclipseModelUtils.collectEclipseModels(ExtendedEclipseModelUtils.queryModels(connection)).values(), monitor);
            this.locationToProject = rootModels.stream()
                .flatMap(p -> HierarchicalElementUtils.getAll(p).stream())
                .collect(Collectors.toMap(p -> p.getProjectDirectory(), p -> p));
        } catch (Exception e) {
            context.error("Cannot Query Eclipse model", e);
        }
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        IProject project = context.getProject();
        try {
            EclipseProject model = lookupEclipseModel(project);
            updateWtpConfiguration(context, JavaCore.create(project), model, this.gradleBuild, monitor);
        } catch (CoreException e) {
            context.error("Failed to configure WTP for project " + project.getName(), e);
        }
    }

    private EclipseProject lookupEclipseModel(IProject project) {
        // TODO (donat) duplicate
        IPath path = project.getLocation();
        if (path == null) {
            return null;
        }
        return this.locationToProject.get(path.toFile());
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
    }

    private static void updateWtpConfiguration(ProjectContext context, IJavaProject javaProject, EclipseProject project, InternalGradleBuild gradleBuild, IProgressMonitor monitor) throws JavaModelException {
        if (CorePlugin.workspaceOperations().isWtpInstalled()) {
            try {
                List<EclipseExternalDependency> dependencies = Lists.newArrayList(project.getClasspath());
                String deploymentPath = getDeploymentPath(context, dependencies);
                if (deploymentPath != null) {
                    updateDeploymentPath(javaProject, deploymentPath, monitor);
                } else if (hasNonDeploymentAttributes(dependencies)) {
                    markAsNonDeployed(javaProject, monitor);
                }
            } catch (UnsupportedConfigurationException e) {
                GradleErrorMarker.createError(javaProject.getProject(), gradleBuild, e.getMessage(), null, 0);
            }
        }
    }

    private static String getDeploymentPath(ProjectContext context, List<EclipseExternalDependency> dependencies) {
        String deploymentPath = null;
        for (EclipseExternalDependency dependency : dependencies) {
            for (ClasspathAttribute attribute : dependency.getClasspathAttributes()) {
                if (attribute.getName().equals(DEPLOYMENT_ATTRIBUTE)) {
                    if (deploymentPath != null && !deploymentPath.equals(attribute.getValue())) {
                        context.error("WTP currently does not support mixed deployment paths.", null);
                        return null;
                    }
                    deploymentPath = attribute.getValue();
                }
            }
        }
        return deploymentPath;
    }

    private static boolean hasNonDeploymentAttributes(List<EclipseExternalDependency> dependencies) {
        for (EclipseExternalDependency dependency : dependencies) {
            for (ClasspathAttribute attribute : dependency.getClasspathAttributes()) {
                if (attribute.getName().equals(NON_DEPLOYMENT_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void updateDeploymentPath(IJavaProject javaProject, String deploymentPath, IProgressMonitor monitor) throws JavaModelException {
        replaceGradleClasspathContainerAttribute(javaProject, DEPLOYMENT_ATTRIBUTE, deploymentPath, NON_DEPLOYMENT_ATTRIBUTE, monitor);
    }

    private static void markAsNonDeployed(IJavaProject javaProject, IProgressMonitor monitor) throws JavaModelException {
        replaceGradleClasspathContainerAttribute(javaProject, NON_DEPLOYMENT_ATTRIBUTE, "", DEPLOYMENT_ATTRIBUTE, monitor);
    }

    private static void replaceGradleClasspathContainerAttribute(IJavaProject project, String plusKey, String plusValue, String minusKey, IProgressMonitor monitor)
            throws JavaModelException {
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
        project.setRawClasspath(newClasspath, monitor);
    }

    private static boolean isGradleClasspathContainer(IClasspathEntry entry) {
        return entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(GradleClasspathContainer.CONTAINER_PATH);
    }

    private static IClasspathAttribute[] replaceClasspathAttribute(IClasspathAttribute[] attributes, String plusKey, String plusValue, String minusKey) {
        List<IClasspathAttribute> attributesList = Lists.newArrayList(attributes);
        ListIterator<IClasspathAttribute> iterator = attributesList.listIterator();
        boolean plusPresent = false;
        while (iterator.hasNext()) {
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
