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

package org.eclipse.buildship.core.util.collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * This {@link Function} is used to get an {@link IProject} instance from a
 * given object by using the {@link IAdapterManager}.
 */
public class ProjectFunction implements Function<Object, IProject> {

    private IAdapterManager adapterManager;

    public ProjectFunction(IAdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public IProject apply(Object input) {
        return getProject(input, this.adapterManager).orNull();
    }

    /**
     * Get an {@link IProject} from an adaptable object.
     *
     * @param adaptable
     *            adaptable to get the {@link IProject} from
     * @param adapterManager
     *            to get proper adapter in order to get the {@link IProject}
     * @return {@link Optional} of an {@link IProject}
     */
    public static Optional<IProject> getProject(Object adaptable, IAdapterManager adapterManager) {
        Optional<IProject> project = Optional.absent();
        @SuppressWarnings({ "cast", "RedundantCast" })
        IResource resource = (IResource) adapterManager.getAdapter(adaptable, IResource.class);
        if (resource != null) {
            project = Optional.fromNullable(resource.getProject());
        } else {
            @SuppressWarnings({ "cast", "RedundantCast" })
            IJavaElement javaElement = (IJavaElement) adapterManager.getAdapter(adaptable, IJavaElement.class);
            if (javaElement != null) {
                IJavaProject javaProject = javaElement.getJavaProject();
                if (javaProject != null) {
                    project = Optional.fromNullable(javaProject.getProject());
                }
            }
        }
        return project;
    }

}
