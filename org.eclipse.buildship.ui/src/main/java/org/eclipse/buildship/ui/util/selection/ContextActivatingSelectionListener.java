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

package org.eclipse.buildship.ui.util.selection;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Dynamically activates the context with the specified id whenever the current selection matches
 * the given {@link Predicate}.
 */
public final class ContextActivatingSelectionListener implements ISelectionListener {

    private final Predicate<? super ISelection> activationPredicate;
    private final String contextId;
    private final IContextService contextService;
    private IContextActivation activation;

    @SuppressWarnings({ "cast", "RedundantCast" })
    public ContextActivatingSelectionListener(String contextId, Predicate<? super ISelection> activationPredicate, IServiceLocator serviceLocator) {
        this.activationPredicate = Preconditions.checkNotNull(activationPredicate);
        this.contextId = Preconditions.checkNotNull(contextId);
        this.contextService = (IContextService) serviceLocator.getService(IContextService.class);
        this.activation = null;
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (this.activationPredicate.apply(selection)) {
            activateContext();
        } else {
            deactivateContext();
        }
    }

    private void activateContext() {
        if (this.contextId != null) {
            this.activation = this.contextService.activateContext(this.contextId);
        }
    }

    private void deactivateContext() {
        if (this.activation != null) {
            this.contextService.deactivateContext(this.activation);
        }
    }

}
