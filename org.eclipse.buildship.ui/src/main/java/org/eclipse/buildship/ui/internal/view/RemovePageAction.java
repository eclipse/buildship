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

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * Removes the target {@link Page} from the {@link MultiPageView} to which this page belongs.
 */
public class RemovePageAction extends Action {

    private final Page page;

    public RemovePageAction(Page page, String tooltip) {
        this.page = Preconditions.checkNotNull(page);

        setToolTipText(tooltip);
        setImageDescriptor(PluginImages.REMOVE_PAGE.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_PAGE.withState(ImageState.DISABLED).getImageDescriptor());
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
        if (this.page.isCloseable()) {
            view.removePage(this.page);
        }
    }

}
