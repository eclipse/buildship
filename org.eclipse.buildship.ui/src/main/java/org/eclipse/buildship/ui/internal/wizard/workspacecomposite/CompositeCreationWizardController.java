/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.collections.CollectionsUtils;
import org.eclipse.buildship.core.internal.workspace.InternalGradleBuild;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
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
        Property<List<File>> compositeProjectsProperty = Property.create(Validators.<List<File>>nullValidator());

        this.configuration = new CompositeCreationConfiguration(compositeNameProperty, compositeProjectsProperty);

        IDialogSettings dialogSettings = compositeCreationWizard.getDialogSettings();
        String compositeName = dialogSettings.get(SETTINGS_KEY_COMPOSITE_NAME);
        List<File> compositeProjects = ImmutableList.copyOf(getIncludedBuildsList(CollectionsUtils.nullToEmpty(dialogSettings.getArray(SETTINGS_KEY_COMPOSITE_PROJECTS))));

        this.configuration.setCompositeName(compositeName);
        this.configuration.setCompositeProjects(compositeProjects);
    }

    public CompositeCreationWizardController(String compositeName, List<File> compositeProjects) {
        // assemble configuration object that serves as the data model of the wizard
        Property<String> compositeNameProperty = Property.create(Validators.uniqueWorkspaceCompositeNameValidator(WorkspaceCompositeWizardMessages.Label_CompositeName));
        Property<List<File>> compositeProjectsProperty = Property.create(Validators.<List<File>>nullValidator());

        this.configuration = new CompositeCreationConfiguration(compositeNameProperty, compositeProjectsProperty);

        this.configuration.setCompositeName(compositeName);
        this.configuration.setCompositeProjects(compositeProjects);
    }

    private List<File> getIncludedBuildsList(String[] projectArray) {
        List<File> includedBuilds = new ArrayList<>();
        for (String projectName : projectArray) {
        	includedBuilds.add(getGradleRootFor(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName)));
        }
        return includedBuilds;
    }

    protected File getGradleRootFor(IProject project) {
		InternalGradleBuild gradleBuild = (InternalGradleBuild) CorePlugin.internalGradleWorkspace().getBuild(project).get();
		return gradleBuild.getBuildConfig().getRootProjectDirectory();
    }

    public CompositeCreationConfiguration getConfiguration() {
        return this.configuration;
    }
}
