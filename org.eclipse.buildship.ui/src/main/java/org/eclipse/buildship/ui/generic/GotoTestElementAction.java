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

package org.eclipse.buildship.ui.generic;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;

import org.eclipse.buildship.ui.i18n.UiMessages;

/**
 * This actions runs the {@link OpenTestCompilationUnitJob}, which navigates to the source of a test
 * according to the ISelection.
 *
 */
public class GotoTestElementAction extends Action {

    private ISelectionProvider selectionProvider;
    private Display display;

    public GotoTestElementAction(ISelectionProvider selectionProvider, Display display) {
        super(UiMessages.Action_OpenTestSourceFile_Text);
        this.selectionProvider = selectionProvider;
        this.display = display;
    }

    @Override
    public void run() {
        OpenTestCompilationUnitJob openTestCompilationUnitJob = new OpenTestCompilationUnitJob(selectionProvider.getSelection(), display);
        openTestCompilationUnitJob.schedule();
    }

}
