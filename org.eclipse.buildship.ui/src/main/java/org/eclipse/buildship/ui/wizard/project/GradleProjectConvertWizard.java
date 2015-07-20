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

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.Wizard;

/**
 * This wizard shows the {@link GradleOptionsWizardPage}, in order to configure the Gradle specific
 * attributes for the conversion.
 *
 */
public class GradleProjectConvertWizard extends Wizard {

    private static final String CONVERT_PROJECT_DIALOG_SETTINGS = "convertProjectDialogSettings"; //$NON-NLS-1$

    private ProjectImportWizardController controller;
    private GradleOptionsWizardPage gradleOptionsPage;

    public GradleProjectConvertWizard() {
        this(getOrCreateDialogSection(UiPlugin.getInstance().getDialogSettings()), CorePlugin.publishedGradleVersions());
    }

    public GradleProjectConvertWizard(IDialogSettings dialogSettings, PublishedGradleVersions publishedGradleVersions) {
        Preconditions.checkNotNull(dialogSettings);
        Preconditions.checkNotNull(publishedGradleVersions);

        setDialogSettings(dialogSettings);

        setController(new ProjectImportWizardController(this));
        // instantiate the pages and pass the configuration object that serves as the data model of
        // the wizard
        ProjectImportConfiguration configuration = getController().getConfiguration();
        this.gradleOptionsPage = new GradleOptionsWizardPage(configuration, publishedGradleVersions);
        this.gradleOptionsPage.setTitle("Convert to Gradle");
        this.gradleOptionsPage.setMessage("Settings for the conversion to Gradle");
    }

    @Override
    public void addPages() {
        addPage(this.gradleOptionsPage);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    private static IDialogSettings getOrCreateDialogSection(IDialogSettings dialogSettings) {
        // in Eclipse 3.6 the method DialogSettings#getOrCreateSection does not exist
        IDialogSettings section = dialogSettings.getSection(CONVERT_PROJECT_DIALOG_SETTINGS);
        if (section == null) {
            section = dialogSettings.addNewSection(CONVERT_PROJECT_DIALOG_SETTINGS);
        }
        return section;
    }

    public ProjectImportWizardController getController() {
        return this.controller;
    }

    public void setController(ProjectImportWizardController controller) {
        this.controller = controller;
    }
}
