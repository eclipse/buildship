/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - adaptation and customization for workspace composite wizard 
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.preferences.GradleWorkbenchPreferencePage;
import org.eclipse.buildship.ui.internal.util.widget.GradleProjectSettingsComposite;
import org.eclipse.buildship.ui.internal.wizard.project.ProjectImportWizard;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Page on the {@link WorkspaceCompositeCreationWizard} declaring the used Gradle distribution and other advanced options for the composite projects.
 */
public final class GradleImportOptionsWizardPage extends AbstractWizardPage {

    private final String pageContextInformation;

    private GradleProjectSettingsComposite gradleProjectSettingsComposite;

    public GradleImportOptionsWizardPage() {
        this(WorkspaceCompositeWizardMessages.Title_GradleImportOptionsWizardPage, WorkspaceCompositeWizardMessages.InfoMessage_NewGradleWorkspaceCompositeOptionsWizardPageDefault,
                WorkspaceCompositeWizardMessages.InfoMessage_GradleImportOptionsWizardPageContext);
    }

    public GradleImportOptionsWizardPage(String title, String defaultMessage, String pageContextInformation) {
    	super("GradleImportOptions", title, defaultMessage);
        this.pageContextInformation = pageContextInformation;
	}

	@Override
    protected void createWidgets(Composite root) {
        GridLayoutFactory.swtDefaults().applyTo(root);
        this.gradleProjectSettingsComposite = GradleProjectSettingsComposite.builder(root)
                .withOverrideCheckbox("Override workspace settings", "Configure Workspace Settings")
                .withAutoSyncCheckbox()
                .build();

        GridDataFactory.fillDefaults().grab(true, false).applyTo(this.gradleProjectSettingsComposite);
        this.gradleProjectSettingsComposite.getParentPreferenceLink().addSelectionListener(new WorkbenchPreferenceOpeningSelectionListener());
    }

    @Override
    protected String getPageContextInformation() {
        return this.pageContextInformation;
    }

    /**
     * Opens the workspace preferences dialog.
     */
    private class WorkbenchPreferenceOpeningSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            openWorkspacePreferences();
        }

        private void openWorkspacePreferences() {
            PreferencesUtil.createPreferenceDialogOn(getShell(), GradleWorkbenchPreferencePage.PAGE_ID, null, null).open();
        }
    }
}
