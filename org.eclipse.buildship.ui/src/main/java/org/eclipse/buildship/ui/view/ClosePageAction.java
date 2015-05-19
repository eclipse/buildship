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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Class to demonstrate how to implement an action for an {@link IPageBookViewPage}.
 */
public final class ClosePageAction extends Action {

    private final IPageBookViewPage page;
    private final MultiPageManager manager;

    public ClosePageAction(MultiPageManager manager, IPageBookViewPage page) {
        this.manager = manager;
        this.page = page;

        setText("Close");
    }

    @Override
    public void run() {
        this.manager.removePage(this.page);

    }
}
