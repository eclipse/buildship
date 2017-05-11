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

package org.eclipse.buildship.ui.console;

import com.google.common.base.Optional;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.ui.view.execution.OpenBuildScanAction;

/**
 * Contributes actions to {@link GradleConsole} instances at the time a new console is initialized.
 */
@SuppressWarnings("unchecked") // Eclipse Mars M6 introduced type parameters on the IAdaptable interface
public final class GradleConsolePageParticipant implements IConsolePageParticipant {

    private CancelBuildExecutionAction cancelBuildExecutionAction;
    private RerunBuildExecutionAction rerunBuildExecutionAction;
    private RemoveTerminatedGradleConsoleAction removeConsoleAction;
    private RemoveAllTerminatedGradleConsolesAction removeAllConsolesAction;
    private OpenBuildScanAction openBuildScanAction;

    /**
     * {@inheritDoc}
     * <p/>
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
        this.rerunBuildExecutionAction = new RerunBuildExecutionAction(gradleConsole);
        this.removeConsoleAction = new RemoveTerminatedGradleConsoleAction(gradleConsole);
        this.removeAllConsolesAction = new RemoveAllTerminatedGradleConsolesAction(gradleConsole);

        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.cancelBuildExecutionAction);
        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.rerunBuildExecutionAction);
        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.removeConsoleAction);
        toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.removeAllConsolesAction);
        Optional<ProcessDescription> description = gradleConsole.getProcessDescription();
        if (description.isPresent()) {
            this.openBuildScanAction = new OpenBuildScanAction(description.get());
            toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.openBuildScanAction);
        }
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
        if (this.openBuildScanAction != null) {
            this.openBuildScanAction.dispose();
            this.openBuildScanAction = null;
        }
        if (this.cancelBuildExecutionAction != null) {
            this.cancelBuildExecutionAction.dispose();
            this.cancelBuildExecutionAction = null;
        }
        if (this.rerunBuildExecutionAction != null) {
            this.rerunBuildExecutionAction.dispose();
            this.rerunBuildExecutionAction = null;
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
