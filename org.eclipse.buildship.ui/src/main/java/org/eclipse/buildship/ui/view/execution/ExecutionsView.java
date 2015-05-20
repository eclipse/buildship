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

package org.eclipse.buildship.ui.view.execution;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * A view displaying the Gradle executions.
 */
public final class ExecutionsView extends ViewPart {

    // view id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.ui.views.executionview"; //$NON-NLS-1$

    private ExecutionsViewState state;

    private PageBook pages;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new ExecutionsViewState();
        this.state.load();
    }

    @Override
    public void createPartControl(Composite parent) {
        // the top-level control changing its content depending on whether the content provider
        // contains execution data to display or not
        this.pages = new PageBook(parent, SWT.NONE);

        // if there is no execution data to display, show only a label
        Label emptyInputPage = new Label(this.pages, SWT.NONE);
        emptyInputPage.setText(ExecutionsViewMessages.Label_No_Execution);
        this.pages.showPage(emptyInputPage);
    }

    public void addPage(BuildLaunchRequest buildLaunchRequest, String processName) {
        ExecutionPage executionPage = new ExecutionPage(this.pages, this.state, buildLaunchRequest);
        this.pages.showPage(executionPage.getFilteredTree());
    }

    @Override
    public void setFocus() {
        this.pages.setFocus();
    }

    @Override
    public void dispose() {
        this.state.dispose();
        this.pages.dispose();
        super.dispose();
    }

}
