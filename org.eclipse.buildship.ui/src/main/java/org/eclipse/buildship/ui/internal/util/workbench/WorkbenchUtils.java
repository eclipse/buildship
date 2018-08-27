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

package org.eclipse.buildship.ui.internal.util.workbench;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Contains helper methods related to the Eclipse workbench.
 */
public final class WorkbenchUtils {

    private WorkbenchUtils() {
    }

/**
     * Shows the view with the given id and secondary id in the given mode.
     *
     * @param viewId the id of the view
     * @param secondaryId the secondary id of the view, or {@code null] for no secondary id
     * @param mode the activation mode, must be {@link org.eclipse.ui.IWorkbenchPage#VIEW_ACTIVATE},
     *            {@link org.eclipse.ui.IWorkbenchPage#VIEW_VISIBLE} or
     *            {@link org.eclipse.ui.IWorkbenchPage#VIEW_CREATE}
     * @param <T> the expected type of the view
     * @return the shown view, never null
     * @throws RuntimeException thrown if the view cannot be initialized correctly
     */
    public static <T extends IViewPart> T showView(String viewId, String secondaryId, int mode) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        try {
            @SuppressWarnings("unchecked")
            T view = (T) activeWorkbenchWindow.getActivePage().showView(viewId, secondaryId, mode);
            return view;
        } catch (PartInitException e) {
            throw new RuntimeException(String.format("Cannot show view with id %s and secondary id %s.", viewId, secondaryId), e);
        }
    }

}
