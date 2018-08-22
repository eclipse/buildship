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

package org.eclipse.buildship.ui.internal.util.selection;

import com.google.common.base.Preconditions;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * Dynamically activates the context with the specified id whenever the currently active
 * {@link IWorkbenchPartReference} belongs to the specified {@link IViewPart}.
 */
public final class ContextActivatingViewPartListener implements IPartListener2 {

    private final IViewPart viewPart;
    private final String contextId;
    private final IContextService contextService;
    private IContextActivation activation;

    @SuppressWarnings({"cast", "RedundantCast"})
    public ContextActivatingViewPartListener(String contextId, IViewPart viewPart) {
        this.viewPart = Preconditions.checkNotNull(viewPart);
        this.contextId = Preconditions.checkNotNull(contextId);
        this.contextService = (IContextService) viewPart.getSite().getService(IContextService.class);
        this.activation = null;
    }

    @Override
    public void partActivated(IWorkbenchPartReference partReference) {
        if (SelectionUtils.belongsToViewPart(partReference, this.viewPart)) {
            this.activation = this.contextService.activateContext(this.contextId);
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partReference) {
        if (SelectionUtils.belongsToViewPart(partReference, this.viewPart) && this.activation != null) {
            this.contextService.deactivateContext(this.activation);
        }
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

}
