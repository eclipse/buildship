/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation for project wizard
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - adaptation and customization for workspace composite wizard 
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the project import messages.
 */
public final class WorkspaceCompositeWizardMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeWizardMessages"; //$NON-NLS-1$
    
    public static String Title_GradleImportOptionsWizardPage;
    public static String Title_CompositeRootWizardPage;

    public static String Title_NewGradleWorkspaceCompositeWizardPage;
    public static String Title_NewGradleImportOptionsWizardPage;
    public static String Title_NewGradleCompositeRootWizardPage;

    public static String Label_CompositeName;

    public static String InfoMessage_CompositeRootWizardPageDefault;

    public static String InfoMessage_NewGradleWorkspaceCompositeWizardPageDefault;
    public static String InfoMessage_NewGradleWorkspaceCompositeOptionsWizardPageDefault;
    public static String InfoMessage_NewGradleWorkspaceCompositePreviewWizardPageDefault;

    public static String InfoMessage_GradleImportOptionsWizardPageContext;
    public static String InfoMessage_CompositeRootWizardPageContext;

    public static String InfoMessage_NewGradleWorkspaceCompositeWizardPageContext;
    public static String InfoMessage_NewGradleWorkspaceCompositeImportOptionsWizardPageContext;
    public static String InfoMessage_NewGradleWorkspaceCompositeCompositeRootWizardPageContext;
    
    public static String Group_Label_GradleProjects;
    public static String Button_New_GradleProject;
    public static String Button_Add_ExternalGradleProject;
    
    public static String Label_RootProject;
    public static String Button_Select_RootProject;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, WorkspaceCompositeWizardMessages.class);
    }

    private WorkspaceCompositeWizardMessages() {
    }

}
