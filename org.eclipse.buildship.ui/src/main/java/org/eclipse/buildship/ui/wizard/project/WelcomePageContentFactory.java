/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 473545
 */

package org.eclipse.buildship.ui.wizard.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.eclipse.buildship.ui.wizard.project.WelcomePageContent.PageParagraph;

/**
 * Factory for creating {@link WelcomePageContent} instances for the different project wizards.
 */
public final class WelcomePageContentFactory {

    private WelcomePageContentFactory() {
    }

    public static WelcomePageContent createImportWizardWelcomePageContent() {
        Builder<PageParagraph> paragraphs = ImmutableList.builder();
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Import_Wizard_Paragraph_Title_Smart_Project_Import,
                ProjectWizardMessages.Import_Wizard_Paragraph_Content_Smart_Project_Import));
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Import_Wizard_Paragraph_Title_Gradle_Wrapper,
                ProjectWizardMessages.Import_Wizard_Paragraph_Content_Gradle_Wrapper));
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Import_Wizard_Paragraph_Title_Advanced_Options,
                ProjectWizardMessages.Import_Wizard_Paragraph_Content_Advanced_Options));

        return new WelcomePageContent(
                ProjectWizardMessages.Import_Wizard_Welcome_Page_Name,
                ProjectWizardMessages.Title_GradleWelcomeWizardPage,
                ProjectWizardMessages.InfoMessage_GradleWelcomeWizardPageDefault,
                ProjectWizardMessages.InfoMessage_GradleWelcomeWizardPageContext,
                ProjectWizardMessages.Import_Wizard_Paragraph_Main_Title, paragraphs.build());
    }

    public static WelcomePageContent createCreationWizardWelcomePageContent() {
        Builder<PageParagraph> paragraphs = ImmutableList.builder();
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Creation_Wizard_Paragraph_Title_Smart_Project_Creation,
                ProjectWizardMessages.Creation_Wizard_Paragraph_Content_Smart_Project_Creation));
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Creation_Wizard_Paragraph_Title_Gradle_Wrapper,
                ProjectWizardMessages.Creation_Wizard_Paragraph_Content_Gradle_Wrapper));
        paragraphs.add(new PageParagraph(ProjectWizardMessages.Creation_Wizard_Paragraph_Title_Advanced_Options,
                ProjectWizardMessages.Creation_Wizard_Paragraph_Content_Advanced_Options));

        return new WelcomePageContent(
                ProjectWizardMessages.Creation_Wizard_Welcome_Page_Name,
                ProjectWizardMessages.Title_GradleWelcomeWizardPage,
                ProjectWizardMessages.InfoMessage_NewGradleProjectWelcomeWizardPageDefault,
                ProjectWizardMessages.InfoMessage_NewGradleProjectWelcomeWizardPageContext,
                ProjectWizardMessages.Creation_Wizard_Paragraph_Main_Title, paragraphs.build());
    }

}
