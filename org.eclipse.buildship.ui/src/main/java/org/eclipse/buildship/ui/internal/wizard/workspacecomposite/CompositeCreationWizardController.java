/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;

import com.google.common.collect.ImmutableList;

public class CompositeCreationWizardController {

    private static final String SETTINGS_KEY_COMPOSITE_NAME = "composite_name"; //$NON-NLS-1$
    private static final String SETTINGS_KEY_COMPOSITE_PROJECTS = "composite_projects"; //$NON-NLS-1$

    private final CompositeCreationConfiguration configuration;

    public CompositeCreationWizardController(IWizard compositeCreationWizard) {
        // assemble configuration object that serves as the data model of the wizard
        Property<String> compositeNameProperty = Property.create(Validators.uniqueWorkspaceCompositeNameValidator(WorkspaceCompositeWizardMessages.Label_CompositeName));
        Property<List<IAdaptable>> compositeProjectsProperty = Property.create(Validators.<List<IAdaptable>>nullValidator());

        this.configuration = new CompositeCreationConfiguration(compositeNameProperty, compositeProjectsProperty);

        IDialogSettings dialogSettings = compositeCreationWizard.getDialogSettings();
        String compositeName = dialogSettings.get(SETTINGS_KEY_COMPOSITE_NAME);
        List<IAdaptable> compositeProjects = ImmutableList.copyOf(getProjects(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_COMPOSITE_PROJECTS))));

        this.configuration.setCompositeName(compositeName);
        this.configuration.setCompositeProjects(compositeProjects);
    }

    public CompositeCreationWizardController(String compositeName, List<IAdaptable> compositeProjects) {
        // assemble configuration object that serves as the data model of the wizard
        Property<String> compositeNameProperty = Property.create(Validators.uniqueWorkspaceCompositeNameValidator(WorkspaceCompositeWizardMessages.Label_CompositeName));
        Property<List<IAdaptable>> compositeProjectsProperty = Property.create(Validators.<List<IAdaptable>>nullValidator());

        this.configuration = new CompositeCreationConfiguration(compositeNameProperty, compositeProjectsProperty);

        this.configuration.setCompositeName(compositeName);
        this.configuration.setCompositeProjects(compositeProjects);
    }

    private List<IAdaptable> getProjects(String[] projectArray) {
        List<IAdaptable> projects = new ArrayList<>();
        for (String projectName : projectArray) {
            projects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
        }
        return projects;
    }


    public CompositeCreationConfiguration getConfiguration() {
        return this.configuration;
    }
}
