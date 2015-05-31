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

package org.eclipse.buildship.ui.wizard.project;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.buildship.core.util.string.StringUtils;

/**
 * Validator that checks that a project name is specified and not already used in the workspace.
 */
public final class ProjectNameValidator implements IValidator {

    public static final ProjectNameValidator INSTANCE  = new ProjectNameValidator();

    private ProjectNameValidator() {
    }

    @Override
    public IStatus validate(Object value) {
        String projectName = StringUtils.valueOf(value);
        if (Strings.isNullOrEmpty(projectName)) {
            return ValidationStatus.error(ProjectWizardMessages.ErrorMessage_ProjectName_MustBeSpecified);
        } else if (projectNameAlreadyExistsInWorkspace(projectName)) {
            return ValidationStatus.error(ProjectWizardMessages.ErrorMessage_ProjectName_AlreadyExists);
        } else {
            return Status.OK_STATUS;
        }
    }

    private boolean projectNameAlreadyExistsInWorkspace(final String projectName) {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return FluentIterable.of(projects).anyMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                return projectName.equals(project.getName());
            }
        });
    }

}
