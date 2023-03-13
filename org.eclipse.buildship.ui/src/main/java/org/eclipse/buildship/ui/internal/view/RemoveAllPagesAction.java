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

import com.google.common.base.Preconditions;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.jface.action.Action;

/**
 * Removes all {@link Page} elements from the target {@link MultiPageView}.
 */
public class RemoveAllPagesAction extends Action {

    private final Page page;

    public RemoveAllPagesAction(Page page, String tooltip) {
        this.page = Preconditions.checkNotNull(page);

        setToolTipText(tooltip);
        setImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.DISABLED).getImageDescriptor());
        enableIfCloseable();
    }

    protected Page getPage() {
        return this.page;
    }

    protected void enableIfCloseable() {
        setEnabled(this.page.isCloseable());
    }

    @Override
    public void run() {
        MultiPageView view = (MultiPageView) this.page.getSite().getViewSite().getPart();
        for (Page page : view.getPages()) {
            if (page.isCloseable()) {
                view.removePage(page);
            }
        }
    }

}
