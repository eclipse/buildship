/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.selection;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * Contains helper methods related to selections.
 */
public final class SelectionUtils {

    private SelectionUtils() {
    }

    /**
     * Attempts to select and reveal the specified resource in all parts within the supplied
     * workbench window's active page.
     * <p>
     * Note: this method has been taken from
     * {@code org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#selectAndReveal(IResource, IWorkbenchWindow)}
     * which is available in library {@code org.eclipse.ui.ide:3.7.0.v20110928-1505}.
     *
     * @param resources the resources to be selected and revealed
     * @param window the workbench window to select and reveal the resources
     */
    @SuppressWarnings({"cast", "RedundantCast"}) // Eclipse Mars M6 introduced type parameters on the IAdaptable interface
    public static void selectAndReveal(List<? extends IResource> resources, IWorkbenchWindow window) {
        // validate the input
        if (window == null || resources == null || resources.isEmpty()) {
            return;
        }
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return;
        }

        // get all the view and editor parts
        List<IWorkbenchPart> parts = Lists.newArrayList();
        for (IWorkbenchPartReference ref : page.getViewReferences()) {
            IWorkbenchPart part = ref.getPart(false);
            if (part != null) {
                parts.add(part);
            }
        }
        for (IWorkbenchPartReference ref : page.getEditorReferences()) {
            IWorkbenchPart part = ref.getPart(false);
            if (part != null) {
                parts.add(part);
            }
        }

        // select and reveal
        final ISelection selection = new StructuredSelection(resources);
        for (IWorkbenchPart part : parts) {
            // get the part's ISetSelectionTarget implementation
            ISetSelectionTarget target;
            if (part instanceof ISetSelectionTarget) {
                target = (ISetSelectionTarget) part;
            } else {
                target = (ISetSelectionTarget) part.getAdapter(ISetSelectionTarget.class);
            }

            if (target != null) {
                // select and reveal resource
                final ISetSelectionTarget finalTarget = target;
                window.getShell().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        finalTarget.selectReveal(selection);
                    }
                });
            }
        }
    }

    /**
     * Returns whether the specified part reference belongs to the given view part.
     * <p>
     * Note: this method has been taken from
     * {@code org.eclipse.ui.internal.console.ConsoleView#isThisPart(IWorkbenchPartReference)} which
     * is available in library {@code org.eclipse.ui.console:3.5.0.v20100526}.
     *
     * @param partReference the part reference to check if it is in the view part
     * @param viewPart the view part the view part
     * @return true if the specified part reference is the task view
     */
    public static boolean belongsToViewPart(IWorkbenchPartReference partReference, IViewPart viewPart) {
        if (partReference instanceof IViewReference) {
            IViewReference currentReference = (IViewReference) partReference;
            IViewSite viewSite = viewPart.getViewSite();
            if (viewSite != null) {
                // compare the ids and secondary ids (the ids are never null, the secondary ids can
                // be null)
                if (currentReference.getId().equals(viewSite.getId()) && Objects.equal(currentReference.getSecondaryId(), viewSite.getSecondaryId())) {
                    return true;
                }
            }
        }
        return false;
    }

}
