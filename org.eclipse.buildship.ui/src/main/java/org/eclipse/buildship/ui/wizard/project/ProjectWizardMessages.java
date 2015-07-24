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

package org.eclipse.buildship.ui.wizard.project;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the project import messages.
 */
public final class ProjectWizardMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.wizard.project.ProjectWizardMessages"; //$NON-NLS-1$

    public static String Title_GradleWelcomeWizardPage;
    public static String Title_GradleProjectWizardPage;
    public static String Title_GradleOptionsWizardPage;
    public static String Title_PreviewImportWizardPage;

    public static String Title_NewGradleProjectWizardPage;
    public static String Title_NewGradleProjectPreviewWizardPage;

    public static String Label_ProjectRootDirectory;
    public static String Label_GradleUserHome;
    public static String Label_JavaHome;
    public static String Label_JvmArguments;
    public static String Label_ProgramArguments;

    public static String Label_AdvancedOptions;
    public static String Label_GradleDistribution;
    public static String Label_GradleVersion;
    public static String Label_ProjectStructure;

    public static String Label_ProjectName;
    public static String Group_Label_ProjectLocation;
    public static String Button_UseDefaultLocation;

    public static String CheckButton_Show_Welcomepage_Next_Time;
    public static String Label_CustomLocation;
    public static String Group_Label_WorkingSets;
    public static String Message_TargetProjectDirectory;

    public static String InfoMessage_GradleWelcomeWizardPageImport;
    public static String InfoMessage_GradleWelcomeWizardPageCreation;
    public static String InfoMessage_GradleProjectWizardPageDefault;
    public static String InfoMessage_GradleOptionsWizardPageDefault;
    public static String InfoMessage_PreviewImportWizardPageDefault;

    public static String InfoMessage_NewGradleProjectWizardPageDefault;
    public static String InfoMessage_NewGradleProjectPreviewWizardPageDefault;

    public static String InfoMessage_GradleWelcomeWizardPageImportContext;
    public static String InfoMessage_GradleWelcomeWizardPageCreationContext;
    public static String InfoMessage_GradleProjectWizardPageContext;
    public static String InfoMessage_GradleOptionsWizardPageContext;
    public static String InfoMessage_GradlePreviewWizardPageContext;

    public static String InfoMessage_NewGradleProjectWizardPageContext;
    public static String InfoMessage_NewGradleProjectPreviewWizardPageContext;

    public static String Title_Dialog_Limitations;
    public static String Limitations_Tooltip;
    public static String Limitations_Details_0_1;

    public static String Import_Wizard_Welcome_Page_Name;
    public static String Import_Wizard_Paragraph_Main_Title;
    public static String Import_Wizard_Paragraph_Title_Smart_Project_Import;
    public static String Import_Wizard_Paragraph_Content_Smart_Project_Import;
    public static String Import_Wizard_Paragraph_Title_Gradle_Wrapper;
    public static String Import_Wizard_Paragraph_Content_Gradle_Wrapper;
    public static String Import_Wizard_Paragraph_Title_Advanced_Options;
    public static String Import_Wizard_Paragraph_Content_Advanced_Options;

    public static String Creation_Wizard_Welcome_Page_Name;
    public static String Creation_Wizard_Paragraph_Main_Title;
    public static String Creation_Wizard_Paragraph_Title_Smart_Project_Creation;
    public static String Creation_Wizard_Paragraph_Content_Smart_Project_Creation;
    public static String Creation_Wizard_Paragraph_Title_Gradle_Wrapper;
    public static String Creation_Wizard_Paragraph_Content_Gradle_Wrapper;
    public static String Creation_Wizard_Paragraph_Title_Advanced_Options;
    public static String Creation_Wizard_Paragraph_Content_Advanced_Options;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ProjectWizardMessages.class);
    }

    private ProjectWizardMessages() {
    }

}
