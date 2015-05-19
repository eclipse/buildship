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
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Convenience class to simplify the implementation of the {@link IPageBookViewPage} interface.
 * <p/>
 * Clients only have to implement the {@link #createPageContents(Composite)} method.
 */
public abstract class BasePage extends org.eclipse.ui.part.Page implements IPageBookViewPage {

    private Control rootControl;

    @Override
    public void createControl(Composite parent) {
        this.rootControl = createPageContents(parent);
    }

    @Override
    public Control getControl() {
        return this.rootControl;
    }

    @Override
    public void setFocus() {
        this.rootControl.setFocus();
    }

    public abstract Control createPageContents(Composite parent);

}
