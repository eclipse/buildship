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

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

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
