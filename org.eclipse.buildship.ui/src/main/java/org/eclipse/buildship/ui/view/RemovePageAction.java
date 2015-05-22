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
 * Action removing the target {@link Page} from the container {@link MultiPageView}.
 */
public final class RemovePageAction extends Action {

    private final Page page;

    public RemovePageAction(Page page, String label) {
        this.page = Preconditions.checkNotNull(page);
        setText(label);
        setImageDescriptor(PluginImages.REMOVE_PAGE.withState(ImageState.ENABLED).getImageDescriptor());
    }

    @Override
    public void run() {
        MultiPageView view = (MultiPageView) this.page.getSite().getViewSite().getPart();
        view.removePage(this.page);
    }

}
