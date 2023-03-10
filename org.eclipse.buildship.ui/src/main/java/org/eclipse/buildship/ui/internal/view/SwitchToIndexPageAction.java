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
