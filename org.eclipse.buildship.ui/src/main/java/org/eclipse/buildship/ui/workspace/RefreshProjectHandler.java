/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Ian Stewart-Binks (Red Hat Inc.) - Bug 473862 - F5 key shortcut doesn't refresh project folder contents
 */

package org.eclipse.buildship.ui.workspace;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Refreshes the project configuration of all Gradle projects that belong to the same build as the currently selected Gradle projects.
 */
public final class RefreshProjectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ProjectSynchronizer.execute(event);
        return null;
    }

}
