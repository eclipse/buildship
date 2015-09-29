/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.google.common.base.Optional;

/**
 * This {@link PropertyTester} is used the check the project nature of a given
 * receiver by {@link Properties#projectnature} and whether the receiver
 * contains only one project by {@link Properties#singleprojectselection}.
 */
public class ProjectPropertyTester extends PropertyTester {

    /**
     * These properties can be tested with {@link ProjectPropertyTester}.
     */
    public enum Properties {
        projectnature, singleprojectselection
    }

    @Override
    public boolean test(Object receiver, String propertyString, Object[] args, Object expectedValue) {

        Properties property = Properties.valueOf(propertyString);

        switch (property) {
        case projectnature:
            // Check whether the given receiver has the expected project nature
            Optional<IProject> project = getProject(receiver);
            try {
                return project.isPresent() && project.get().isAccessible()
                        && project.get().hasNature((String) expectedValue);
            } catch (CoreException e) {
                return false;
            }
        case singleprojectselection:
            // used to make sure that only members of one project are selected
            if (receiver instanceof List) {
                return onlyOneProjectIsSelected((List<?>) receiver);
            } else if (receiver instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) receiver;
                return onlyOneProjectIsSelected(selection.toList());
            }
        }

        return true;
    }

    protected Optional<IProject> getProject(Object receiver) {
        Optional<IProject> project = Optional.absent();
        IResource resource = (IResource) Platform.getAdapterManager().getAdapter(receiver, IResource.class);
        if (resource != null) {
            project = Optional.fromNullable(resource.getProject());
        } else {
            IJavaElement javaElement = (IJavaElement) Platform.getAdapterManager().getAdapter(receiver,
                    IJavaElement.class);
            if (javaElement != null) {
                IJavaProject javaProject = javaElement.getJavaProject();
                if (javaProject != null) {
                    project = Optional.fromNullable(javaProject.getProject());
                }
            }
        }
        return project;
    }

    protected boolean onlyOneProjectIsSelected(List<?> list) {
        if (list.isEmpty()) {
            return false;
        }
        Optional<IProject> firstProject = getProject(list.get(0));
        for (Object object : list) {
            Optional<IProject> projectIterate = getProject(object);
            if (!firstProject.equals(projectIterate)) {
                return false;
            }
        }
        return true;
    }

}
