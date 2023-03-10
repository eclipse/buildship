/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.workbench;

import java.util.Optional;

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

    /**
     * Returns the view with the given ID.
     */
    public static <T extends IViewPart> Optional<T> findView(String viewId) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        @SuppressWarnings("unchecked")
        T result = (T) activeWorkbenchWindow.getActivePage().findView(viewId);
        return Optional.ofNullable(result);
    }
}
