/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - initial implemenation
 */

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
