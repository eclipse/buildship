/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.workspace;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class StartupSetup implements IStartup {

    @Override
    public void earlyStartup() {
        IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();

        manager.addPropertyChangeListener(new CompositePropertyChangeListener());
    }

}
