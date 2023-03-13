/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;

/**
 * Page site to modify the page specific {@link IActionBars}.
 */
public interface PageSite {

    /**
     * Returns the site of the container view.
     *
     * @return the container view's site
     */
    IViewSite getViewSite();

    /**
     * Returns the action bars of the page site. Pages have exclusive use of their site's action bars.
     *
     * @return the action bars
     */
    IActionBars getActionBars();

    /**
     * Disposes the page site.
     */
    void dispose();

}
