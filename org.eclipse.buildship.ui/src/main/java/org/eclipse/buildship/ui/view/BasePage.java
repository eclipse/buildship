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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Convenience class partially implementing the {@link Page} interface.
 *
 * @param <T> the type of the root control the basePage should return in the
 *            {@link #getPageControl()} method
 */
public abstract class BasePage<T extends Control> implements Page {

    private PageSite pageSite;
    private T rootControl;

    @Override
    public void init(PageSite pageSite) {
        this.pageSite = pageSite;
    }

    @Override
    public PageSite getSite() {
        return this.pageSite;
    }

    /**
     * Creates the UI widgets and return the root control.
     * <p/>
     * The result will be used by the {@link #getPageControl()} and the {@link #setFocus()}.
     *
     * @param parent the control to attach UI widgets to
     * @return the root control
     */
    public abstract T createPageWithResult(Composite parent);

    @Override
    public void createPage(Composite parent) {
        this.rootControl = createPageWithResult(parent);
    }

    @Override
    public T getPageControl() {
        return this.rootControl;
    }

    @Override
    public void setFocus() {
        this.rootControl.setFocus();
    }

    @Override
    public void dispose() {
    }

    @Override
    public <U> U getAdapter(Class<U> adapter) {
        return null;
    }
}
