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

package org.eclipse.buildship.ui.part;

import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.jface.viewers.Viewer;

/**
 * Abstract implementation of an AbstractPagePart which is an FilteredTreeProvider.
 *
 */
public abstract class FilteredTreePagePart extends AbstractPagePart implements FilteredTreeProvider {

    @Override
    public FilteredTree getFilteredTree() {
        IPage page = getCurrentPage();
        if (page instanceof FilteredTreeProvider) {
            return ((FilteredTreeProvider) page).getFilteredTree();
        }
        return null;
    }

    @Override
    public Viewer getViewer() {
        IPage page = getCurrentPage();
        if (page instanceof ViewerProvider) {
            return ((ViewerProvider) page).getViewer();
        }

        return null;
    }



}
