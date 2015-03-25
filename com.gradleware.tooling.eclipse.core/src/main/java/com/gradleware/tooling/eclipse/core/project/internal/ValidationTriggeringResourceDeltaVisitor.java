/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gradleware.tooling.eclipse.core.project.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public final class ValidationTriggeringResourceDeltaVisitor implements IResourceDeltaVisitor {

    private final GradleProjectValidator gradleProjectValidation;

    public ValidationTriggeringResourceDeltaVisitor(IProject project) {
        this.gradleProjectValidation = new GradleProjectValidator(project);
    }

    public void validate() throws CoreException {
        this.gradleProjectValidation.validateProjectConfigurationExists();
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        if (delta.getKind() == IResourceDelta.ADDED && shouldTriggerValidation(delta.getMovedFromPath())) {
            // handle the use-case when .settings folder is renamed to something else
            validate();
        } else if (shouldTriggerValidation(delta.getProjectRelativePath())) {
            validate();
        }
        return true;
    }

    private boolean shouldTriggerValidation(IPath projectRelativePath) {
        // we want to validate if any interesting resource is modified inside the project
        // therefore we check if the target path is or under any if the interesting resource
        // from the validation
        if (projectRelativePath == null) {
            return false;
        } else {
            for (IResource resource : this.gradleProjectValidation.resourcesToValidate()) {
                if (resource.getProjectRelativePath().isPrefixOf(projectRelativePath)) {
                    return true;
                }
            }
            return false;
        }
    }

}
