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

import org.eclipse.jface.action.Action;

/**
 * This action is able to switch between the pages of an {@link MultiPageView} by a given index.
 *
 * @see SwitchToNextPageAction
 */
public final class SwitchToIndexPageAction extends Action {

    private final MultiPageView pagePart;
    private final int index;

    public SwitchToIndexPageAction(MultiPageView pagePart, String name, int index) {
        super(name);
        this.pagePart = pagePart;
        this.index = index;
    }

    @Override
    public void run() {
        this.pagePart.switchToPageAtIndex(this.index);
    }

}
