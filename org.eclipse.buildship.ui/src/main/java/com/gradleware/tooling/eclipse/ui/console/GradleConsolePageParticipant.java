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

package com.gradleware.tooling.eclipse.ui.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Contributes actions to {@link GradleConsole} instances at the time a new console is initialized.
 */
public final class GradleConsolePageParticipant implements IConsolePageParticipant {

    private CancelBuildExecutionAction cancelBuildExecutionAction;
    private RemoveTerminatedGradleConsoleAction removeConsoleAction;
    private RemoveAllTerminatedConsolesAction removeAllConsolesAction;

    /**
     * {@inheritDoc}
     *
     * Adds custom toolbar items to {@link GradleConsole} instances.
     */
    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        if (console instanceof GradleConsole) {
            GradleConsole gradleConsole = (GradleConsole) console;
            if (gradleConsole.isCloseable()) {
                addActionsToToolbar(page.getSite().getActionBars().getToolBarManager(), gradleConsole);
            }
        }
    }

    private void addActionsToToolbar(IToolBarManager toolBarManager, GradleConsole gradleConsole) {
        this.cancelBuildExecutionAction = new CancelBuildExecutionAction(gradleConsole);
        this.removeConsoleAction = new RemoveTerminatedGradleConsoleAction(gradleConsole);
        this.removeAllConsolesAction = new RemoveAllTerminatedConsolesAction(gradleConsole);

        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.cancelBuildExecutionAction);
        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.removeConsoleAction);
        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.removeAllConsolesAction);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public void activated() {
        // do nothing
    }

    @Override
    public void deactivated() {
        // do nothing
    }

    @Override
    public void dispose() {
        if (this.cancelBuildExecutionAction != null) {
            this.cancelBuildExecutionAction.dispose();
            this.cancelBuildExecutionAction = null;
        }
        if (this.removeConsoleAction != null) {
            this.removeConsoleAction.dispose();
            this.removeConsoleAction = null;
        }
        if (this.removeAllConsolesAction != null) {
            this.removeAllConsolesAction.dispose();
            this.removeAllConsolesAction = null;
        }
    }

}
