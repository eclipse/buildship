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

package org.eclipse.buildship.ui.wizard.project.internal;

import java.util.List;

import org.eclipse.buildship.ui.wizard.project.WelcomePageContent;

import com.google.common.base.Preconditions;

/**
 * Default implementation of a {@link WelcomePageContent}.
 */
public final class DefaultWelcomePageContent implements WelcomePageContent {

    private String name;
    private String title;
    private String message;
    private String pageContextInformation;
    private String welcomePageParagraphtitle;
    private List<WelcomePageParagraph> welcomePageParagraphs;

    public DefaultWelcomePageContent(String name, String title, String message, String pageContextInformation,
            String welcomePageParagraphtitle, List<WelcomePageParagraph> welcomePageParagraphs) {
        this.name = Preconditions.checkNotNull(name);
        this.title = Preconditions.checkNotNull(title);
        this.message = Preconditions.checkNotNull(message);
        this.pageContextInformation = Preconditions.checkNotNull(pageContextInformation);
        this.welcomePageParagraphtitle = Preconditions.checkNotNull(welcomePageParagraphtitle);
        this.welcomePageParagraphs = Preconditions.checkNotNull(welcomePageParagraphs);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getPageContextInformation() {
        return this.pageContextInformation;
    }

    @Override
    public String getWelcomePageParagraphTitle() {
        return this.welcomePageParagraphtitle;
    }

    @Override
    public List<WelcomePageParagraph> getWelcomePageParagraphs() {
        return this.welcomePageParagraphs;
    }

}
