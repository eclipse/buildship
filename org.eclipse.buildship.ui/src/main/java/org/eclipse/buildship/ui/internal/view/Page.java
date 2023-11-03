/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for pages which are shown inside a {@link MultiPageView}.
 * <p/>
 * Implementations can either implement this interface directly or inherit from the {@link BasePage}
 * class containing sensible defaults.
 *
 * @see org.eclipse.buildship.ui.internal.view.BasePage
 * @see org.eclipse.buildship.ui.internal.view.MultiPageView
 */
public interface Page extends IAdaptable {

    /**
     * Returns the name of the page.
     *
     * @return the name of the page
     */
    String getDisplayName();

    /**
     * Creates the UI widgets of the page under the given parent control.
     *
     * @param parent the parent control to add the widgets to
     */
    void createPage(Composite parent);

    /**
     * Returns the root control of the page's UI widgets.
     * <p/>
     * If the page has not yet been created this method returns {@code null}.
     *
     * @return the root control or {@code null} if the page has not been created yet
     */
    Control getPageControl();

    /**
     * Associates the given site with the page and initializes it.
     *
     * @param pageSite the site
     */
    void init(PageSite pageSite);

    /**
     * Returns the {@link PageSite} instance associated with this page.
     * <p/>
     * If the page has not yet been initialized this method returns {@code null}.
     *
     * @return the associated site or {@code null} if the page has not been initialized yet
     */
    PageSite getSite();

    /**
     * Returns whether the page is in a state that it can be closed.
     *
     * @return {@code true} if the page can be closed
     */
    boolean isCloseable();

    /**
     * Sets the focus on the main control of the page.
     * <p/>
     * If the page has not yet been created this method does nothing.
     */
    void setFocus();

    /**
     * Disposes the resources of the page.
     */
    void dispose();

}
