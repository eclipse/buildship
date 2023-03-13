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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Base class partially for {@link Page} implementations.
 *
 * @param <T> the type of the root control of the page
 */
public abstract class BasePage<T extends Control> implements Page {

    private PageSite pageSite;
    private T rootControl;

    @Override
    public void createPage(Composite parent) {
        this.rootControl = createPageWithResult(parent);
    }

    protected abstract T createPageWithResult(Composite parent);

    @Override
    public T getPageControl() {
        return this.rootControl;
    }

    @Override
    public void init(PageSite pageSite) {
        this.pageSite = pageSite;
    }

    @Override
    public PageSite getSite() {
        return this.pageSite;
    }

    @Override
    public boolean isCloseable() {
        return true;
    }

    @Override
    public void setFocus() {
        if (this.rootControl != null) {
            this.rootControl.setFocus();
        }
    }

    @Override
    public void dispose() {
        if (this.rootControl != null) {
            this.rootControl.dispose();
        }
    }

}
