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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.projectimport.ProjectCreatedEvent;
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
        final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet[] workingSets = FluentIterable.from(event.getWorkingSets()).transform(new Function<String, IWorkingSet>() {
            @Override
            public IWorkingSet apply(String name) {
                return workingSetManager.getWorkingSet(name);
            }
        }).filter(Predicates.notNull()).toArray(IWorkingSet.class);
        workingSetManager.addToWorkingSets(event.getProject(), workingSets);
    }

}
