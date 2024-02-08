/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * This is a drop down action, which switches to the next page of a {@link MultiPageView} and
 * also contains a menu with {@link SwitchToIndexPageAction} action, so that it is also possible to
 * switch to a certain page by index.
 */
public final class SwitchToNextPageAction extends Action implements IMenuCreator {

    private final MultiPageView multiPageView;
    private MenuManager menuManager;

    public SwitchToNextPageAction(MultiPageView multiPageView, String toolTip) {
        super(null, AS_DROP_DOWN_MENU);
        this.multiPageView = multiPageView;

        setToolTipText(toolTip);
        setImageDescriptor(PluginImages.SWITCH_PAGE.withState(ImageState.ENABLED).getImageDescriptor());
        setMenuCreator(this);
    }

    @Override
    public void run() {
        this.multiPageView.switchToNextPage();
    }

    @Override
    public Menu getMenu(Control parent) {
        if (this.menuManager != null) {
            this.menuManager.dispose();
        }

        this.menuManager = new MenuManager();
        this.menuManager.createContextMenu(parent);

        // add an entry to the drop down menu for each page
        Page currentPage = this.multiPageView.getCurrentPage();
        List<Page> pages = this.multiPageView.getPages();
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            SwitchToIndexPageAction switchToIndexPageAction = new SwitchToIndexPageAction(page.getDisplayName(), i, this.multiPageView);
            switchToIndexPageAction.setChecked(page.equals(currentPage));
            this.menuManager.add(switchToIndexPageAction);
        }

        return this.menuManager.getMenu();
    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

    @Override
    public void dispose() {
        if (this.menuManager != null) {
            this.menuManager.dispose();
        }
    }

}
