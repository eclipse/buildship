/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.eclipse.buildship.ui.internal.workspace.RefreshProjectAction;

/**
 * Contributes items to the toolbar when {@link GradleEditor} is opened.
 *
 * @author Christophe Moine
 */
public class GradleEditorContributor extends EditorActionBarContributor {

    @Override
    public void contributeToToolBar(IToolBarManager toolBarManager) {
        toolBarManager.add(new RefreshProjectAction());
    }
}
