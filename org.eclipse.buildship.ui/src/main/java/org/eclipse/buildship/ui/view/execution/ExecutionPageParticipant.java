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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;

import org.eclipse.buildship.ui.view.ClosePageAction;
import org.eclipse.buildship.ui.view.PageParticipant;
import org.eclipse.buildship.ui.view.PageParticipantFactory;

/**
 * Here comes the documentation.
 */
public class ExecutionPageParticipant implements PageParticipant {

    public static PageParticipantFactory factory() {
        return new PageParticipantFactory() {

            @Override
            public PageParticipant newParticipant() {
                return new ExecutionPageParticipant();
            }
        };
    }

    @Override
    public void init(IPageSite site, IPageBookViewPage page) {
        if (page instanceof ExecutionPage) {
            ExecutionPage executionPage = (ExecutionPage) page;
            IToolBarManager manager = site.getActionBars().getToolBarManager();
            manager.add(new ClosePageAction(executionPage.getExecutionsView().getManager(), executionPage));
        }
    }

    @Override
    public void dispose() {
    }

}
