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

package org.eclipse.buildship.ui.workbench;

import org.eclipse.buildship.core.workbench.WorkbenchOperations;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Default implementation of the {@code WorkbenchOperations} interface.
 */
public final class DefaultWorkbenchOperations implements WorkbenchOperations {

    private static final String TEST_RUNNER_VIEW_ID = "org.eclipse.jdt.junit.ResultView";

    @Override
    public void activateTestRunnerView() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                WorkbenchUtils.showView(TEST_RUNNER_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
            }
        });
    }

}
