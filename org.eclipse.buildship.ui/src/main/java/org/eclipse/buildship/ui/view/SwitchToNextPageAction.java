/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * This is a drop down action, which switches to the next page of a {@link MultiPageView} and
 * also contains a menu with {@link SwitchToIndexPageAction} action, so that it is also possible to
 * switch to a certain page by index.
 *
 */
public final class SwitchToNextPageAction extends Action implements IMenuCreator {

    private final MultiPageView pagePart;
    private MenuManager menuManager;

    public SwitchToNextPageAction(MultiPageView pagePart) {
        super("Switch View", AS_DROP_DOWN_MENU);
        setImageDescriptor(PluginImages.SWITCH_PAGE.withState(ImageState.ENABLED).getImageDescriptor());
        this.pagePart = pagePart;
        setMenuCreator(this);
    }

    @Override
    public void run() {
        this.pagePart.switchToNextPage();
    }

    @Override
    public void dispose() {
        this.menuManager.dispose();
    }

    @Override
    public Menu getMenu(Control parent) {
        if (this.menuManager != null) {
            this.menuManager.dispose();
        }

        this.menuManager = new MenuManager();
        this.menuManager.createContextMenu(parent);

        List<Page> pages = this.pagePart.getPages();
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            SwitchToIndexPageAction switchToIndexPageAction = new SwitchToIndexPageAction(this.pagePart, page.getDisplayName(), i);
            if (page.equals(this.pagePart.getCurrentPage())) {
                switchToIndexPageAction.setChecked(true);
            }
            this.menuManager.add(switchToIndexPageAction);
        }

        return this.menuManager.getMenu();
    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

}
