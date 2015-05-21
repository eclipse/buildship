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

package org.eclipse.buildship.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for pages, which are shown inside an
 * {@link org.eclipse.buildship.ui.view.MultiPageView.AbstractPagePart}.
 * <p/>
 * Implementations can either implement this interface directly or inherit from the {@link BasePage}
 * class containing sensible defaults.
 */
public interface Page extends IAdaptable {

    /**
     * Returns the name of the page.
     *
     * @return the name of the page
     */
    String getDisplayName();

    /**
     * Creates the UI widgets for the page.
     *
     * @param parent control to add the widgets
     */
    void createPage(Composite parent);

    /**
     * Returns the root control of the page widgets.
     * <p/>
     * If the control is not yet created this method then returns {@code null}.
     *
     * @return the root control or {@code null} if not initialized
     */
    Control getPageControl();

    /**
     * When the page gains focus this method should propagate this request and set the focus on the
     * main control defined inside. Does nothing when the UI widgets hasn't been created.
     */
    void setFocus();

    /**
     * Associate the site with the current page and initialize it.
     *
     * @param pageSite the target site
     */
    void init(PageSite pageSite);

    /**
     * Returns the {@link PageSite} instance associated with this page. Returns {@code null} if the
     * {@link #init(PageSite)} method ha not been called.
     *
     * @return the associated site
     */
    PageSite getSite();

    /**
     * Disposes the resources when the Page is removed from the view.
     */
    void dispose();
}
