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
