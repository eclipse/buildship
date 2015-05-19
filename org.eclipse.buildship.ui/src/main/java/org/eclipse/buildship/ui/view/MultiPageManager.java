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

package org.eclipse.buildship.ui.view;

import java.util.List;

import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Page manipulation facility for {@link org.eclipse.buildship.ui.view.MultiPageView}.
 * <p/>
 * An instance can be acquired from {@link MultiPageView#getManager()}. Each instance of this
 * interface is directly associated to the creator view.
 */
public interface MultiPageManager {

    /**
     * Registers a new {@link PageParticipantFactory instance for the view associated to the
     * manager.
     * <p/>
     * After the factory is registered a new PageParticipant instance will be created and associate
     * with the new pages. When a page is created or disposed the corresponding
     * PageParticipant#init(org.eclipse.ui.part.IPageSite, IPageBookViewPage)} and
     * PageParticipant#dispose() is called.
     *
     * @param participantFactory the factory used to create PageParticioant instances for each view
     */
    void registerPageParticipantFactory(PageParticipantFactory participantFactory);

    /**
     * Adds a new page to the view.
     *
     * @param page the target page
     */
    void addPage(IPageBookViewPage page);

    /**
     * Removes an existing page from the view. Does nothing when the page doesn't exist already.
     *
     * @param page the target page
     */
    void removePage(IPageBookViewPage page);

    /**
     * Returns the currently visible page from the view. If no pages present then the return value
     * is {@code null}.
     *
     * @return the current page
     */
    IPageBookViewPage currentPage();

    /**
     * Return the list containing all pages in the view.S
     *
     * @return the list of pages from the view
     */
    List<IPageBookViewPage> allPages();
}
