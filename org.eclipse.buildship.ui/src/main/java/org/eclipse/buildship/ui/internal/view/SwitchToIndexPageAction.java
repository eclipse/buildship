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

import org.eclipse.jface.action.Action;

/**
 * An action to switch to a page of a {@link MultiPageView} by the index of the page.
 */
public final class SwitchToIndexPageAction extends Action {

    private final int index;
    private final MultiPageView multiPageView;

    public SwitchToIndexPageAction(String text, int index, MultiPageView multiPageView) {
        super(text);

        this.index = index;
        this.multiPageView = multiPageView;
    }

    @Override
    public void run() {
        this.multiPageView.switchToPageAtIndex(this.index);
    }

}
