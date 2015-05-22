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
 * Removes all {@link Page} elements from the target {@link MultiPageView}.
 */
public final class RemoveAllPagesAction extends Action {

    private final MultiPageView view;

    public RemoveAllPagesAction(MultiPageView view, String text) {
        this.view = Preconditions.checkNotNull(view);

        setText(text);
        setImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.DISABLED).getImageDescriptor());
    }

    @Override
    public void run() {
        this.view.removeAllPages();
    }
}
