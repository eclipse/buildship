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

package org.eclipse.buildship.ui.part.execution;

import org.eclipse.buildship.ui.part.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Default {@link org.eclipse.buildship.ui.part.IPage}, which will be shown on the
 * {@link ExecutionsView}, when no build has been started.
 */
public class DefaultExecutionPage implements IPage {

    private Label label;
    private String displayName;

    @Override
    public void createPage(Composite parent) {
        this.label = new Label(parent, SWT.NONE);
        this.label.setText("Please start a build.");
    }

    @Override
    public Control getPageControl() {
        return this.label;
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        if (getPageControl() != null && !getPageControl().isDisposed()) {
            getPageControl().dispose();
            label = null;
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
