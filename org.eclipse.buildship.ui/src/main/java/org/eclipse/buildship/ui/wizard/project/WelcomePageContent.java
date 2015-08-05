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

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;

import com.google.common.base.Preconditions;

/**
 * This interface is used to configure the {@link GradleWelcomeWizardPage}.
 *
 * @see GradleWelcomeWizardPage
 */
public interface WelcomePageContent {

    /**
     * This is the name of the {@link WizardPage}.
     *
     * @return the name of the {@link WizardPage}
     */
    String getName();

    /**
     * This is the title, which is shown in the title area of the
     * {@link WizardPage}.
     *
     * @return title for the {@link WizardPage}
     */
    String getTitle();

    /**
     * This is the message, which is shown below the title in the title area of
     * the {@link WizardPage}.
     *
     * @return message of the title area in the {@link WizardPage}
     */
    String getMessage();

    /**
     * {@link AbstractWizardPage} objects provide to show page context
     * information in the bottom of a {@link WizardPage}.
     *
     * @return the page context information content
     */
    String getPageContextInformation();

    /**
     * The title for the content area of a {@link GradleWelcomeWizardPage}
     *
     * @return content area's title
     */
    String getWelcomePageParagraphTitle();

    /**
     * A list of {@link WelcomePageParagraph} objects, which should be shown in
     * the content of the {@link GradleWelcomeWizardPage}.
     *
     * @return paragraphs for the {@link GradleWelcomeWizardPage}
     */
    List<WelcomePageParagraph> getWelcomePageParagraphs();

    /**
     * Specifies a paragraph, which is shown on the GradleWelcomeWizardPage.
     */
    public final class WelcomePageParagraph {

        private String title;
        private String content;

        public WelcomePageParagraph(String title, String content) {
            this.title = Preconditions.checkNotNull(title);
            this.content = Preconditions.checkNotNull(content);
        }

        public String getTitle() {
            return this.title;
        }

        public String getContent() {
            return this.content;
        }

    }
}
