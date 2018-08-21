/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.eclipse.buildship.ui.workspace.RefreshProjectAction;

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
