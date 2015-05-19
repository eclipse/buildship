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

import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

/**
 * Defines how to add contributions to page in the {@link MultiPageView}.
 */
public interface PageParticipant {

    /**
     * Called when a new page is added to the view.
     *
     * @param site the target site
     * @param page the target page
     */
    void init(IPageSite site, IPageBookViewPage page);

    /**
     * Called when the page is disposed.
     */
    void dispose();
}
