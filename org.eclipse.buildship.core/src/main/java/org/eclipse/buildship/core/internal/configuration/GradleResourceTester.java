/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Determines whether a given {@link IResource} is a Gradle resource, i.e.
 *
 * <ul>
 *  <li>a Gradle project</li>
 *  <li>a .gradle file</li>
 *  <li>a gradle.properties file</li>
 * </ul>
 * @author Stefan Oehme
 *
 */
public class GradleResourceTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IResource) {
            IResource resource = (IResource) receiver;
            IProject project = resource.getProject();
            if (GradleProjectNature.isPresentOn(project)) {
                return resource instanceof IProject || "gradle".equals(resource.getFileExtension()) || "gradle.properties".equals(resource.getName());
            }
        }
        return false;
    }

}
