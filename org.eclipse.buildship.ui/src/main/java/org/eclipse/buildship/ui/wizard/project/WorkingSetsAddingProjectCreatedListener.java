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

package org.eclipse.buildship.ui.wizard.project;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.workspace.ProjectCreatedEvent;
import org.eclipse.buildship.ui.util.workbench.WorkingSetUtils;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * {@link EventListener} implementation that adds the created project to the requested working sets.
 * <p/>
 * The listener implementation is necessary since working sets are a UI-related concept and the
 * project creation is performed in the core component.
 */
public final class WorkingSetsAddingProjectCreatedListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof ProjectCreatedEvent) {
            handleProjectCreatedEvent((ProjectCreatedEvent) event);
        }
    }

    private void handleProjectCreatedEvent(ProjectCreatedEvent event) {
        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet[] workingSets = WorkingSetUtils.toWorkingSets(event.getWorkingSets());
        workingSetManager.addToWorkingSets(event.getProject(), workingSets);
    }

}
