/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import org.eclipse.jface.wizard.Wizard;

import org.eclipse.buildship.ui.internal.wizard.HelpContextIdProvider;

/**
 * Base class for project wizards.
 */
public abstract class AbstractProjectWizard extends Wizard implements HelpContextIdProvider {

    // state bit storing that the wizard is blocked to finish globally
    private boolean finishGloballyEnabled;

    protected AbstractProjectWizard() {
        // the wizard must not be finishable unless this global flag is enabled
        this.finishGloballyEnabled = true;
    }

    @Override
    public boolean canFinish() {
        // the wizard can finish if all pages are complete and the finish is
        // globally enabled
        return super.canFinish() && this.finishGloballyEnabled;
    }

    public void setFinishGloballyEnabled(boolean finishGloballyEnabled) {
        this.finishGloballyEnabled = finishGloballyEnabled;
    }

}
