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

package org.eclipse.buildship.ui.internal.wizard.project;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Describes the content of a welcome page.
 */
public final class WelcomePageContent {

    private String name;
    private String title;
    private String message;
    private String pageContextInformation;
    private String paragraphTitle;
    private List<PageParagraph> paragraphs;

    public WelcomePageContent(String name, String title, String message, String pageContextInformation,
                              String paragraphTitle, List<PageParagraph> paragraphs) {
        this.name = Preconditions.checkNotNull(name);
        this.title = Preconditions.checkNotNull(title);
        this.message = Preconditions.checkNotNull(message);
        this.pageContextInformation = Preconditions.checkNotNull(pageContextInformation);
        this.paragraphTitle = Preconditions.checkNotNull(paragraphTitle);
        this.paragraphs = ImmutableList.copyOf(paragraphs);
    }

    /**
     * The name of the page.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * The title of the page.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * The message shown below the title in the title area of the page.
     *
     * @return the message of the title area
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * The title of the content area of the page.
     *
     * @return the title of the content area
     */
    public String getParagraphTitle() {
        return this.paragraphTitle;
    }

    /**
     * The paragraphs shown in the content area of the page.
     *
     * @return the paragraphs of the content area
     */
    public List<PageParagraph> getParagraphs() {
        return this.paragraphs;
    }

    /**
     * The page context information shown at the bottom of the page.
     *
     * @return the page context information
     */
    public String getPageContextInformation() {
        return this.pageContextInformation;
    }

    /**
     * Describes a single paragraph.
     */
    public static final class PageParagraph {

        private final String title;
        private final String content;

        public PageParagraph(String title, String content) {
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
