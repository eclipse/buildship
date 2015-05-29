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

package org.eclipse.buildship.ui.databinding.validator;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.buildship.ui.wizard.project.ProjectWizardMessages;

/**
 * This {@link IValidator} is used to check whether a {@link String} or an {@link ISelection} is
 * empty.
 *
 */
public class ProjectNameValidator implements IValidator {

    @Override
    public IStatus validate(Object value) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.isEmpty()) {
                return ValidationStatus.error(ProjectWizardMessages.ProjectNameValidator_Error_Project_Name_Empty);
            }
            if (checkAlreadyExists(stringValue)) {
                return ValidationStatus.error(ProjectWizardMessages.ProjectNameValidator_Error_Project_Name_Exists);
            }
        }
        return Status.OK_STATUS;
    }

    private boolean checkAlreadyExists(final String projectName) {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        return FluentIterable.<IProject> of(projects).anyMatch(new Predicate<IProject>() {

            @Override
            public boolean apply(IProject project) {
                if (projectName.equals(project.getName())) {
                    return true;
                }
                return false;
            }
        });
    }

}
