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

package org.eclipse.buildship.ui.view.executionview;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.buildship.ui.view.ViewerPart;
import org.eclipse.buildship.ui.view.pages.IPage;

public abstract class AbstractPagePart extends ViewPart {

    private IPage currentPage;
    private List<IPage> pages = new LinkedList<IPage>();

    private PageSelectionProvider pageSelectionProvider;

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            pageSelectionProvider.setSelection(event.getSelection());
        }
    };

    private Composite stackComposite;
    private StackLayout stackLayout;
    private IPage defaultPage;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        pageSelectionProvider = new PageSelectionProvider();

        site.setSelectionProvider(pageSelectionProvider);
    }

    @Override
    public final void createPartControl(Composite parent) {
        stackComposite = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        stackComposite.setLayout(stackLayout);
        defaultPage = getDefaultPage();
        defaultPage.createPage(stackComposite);
        setCurrentPage(defaultPage);
    }

    protected abstract IPage getDefaultPage();

    public void addPage(IPage page) {
        page.createPage(stackComposite);
        pages.add(page);
    }

    @Override
    public void setFocus() {
        getCurrentPage().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void setCurrentPage(IPage page) {
        Control pageControl = page.getPageControl();
        if (pageControl != null && !pageControl.getParent().equals(stackComposite)) {
            throw new RuntimeException("The given page does not belong to this PagePart.");
        }
        changeInternalSelectionProvider(currentPage, true);
        currentPage = page;
        stackLayout.topControl = pageControl;
        stackComposite.layout();

        // always set the currently shown page as selectionprovider
        changeInternalSelectionProvider(page, false);
    }

    private void changeInternalSelectionProvider(IPage page, boolean remove) {
        if (page instanceof ViewerPart) {
            Viewer viewer = ((ViewerPart) page).getViewer();
            if (viewer != null) {
                if (remove) {
                    viewer.removeSelectionChangedListener(selectionChangedListener);
                } else {
                    viewer.addSelectionChangedListener(selectionChangedListener);
                }
            }
        }
    }

    public void removeAllPages() {
        for (IPage page : getPages()) {
            page.dispose();
        }

        getPages().clear();
        setCurrentPage(defaultPage);
    }

    public void removeCurrentPage() {
        getCurrentPage().dispose();
        getPages().remove(getCurrentPage());

        if (getPages().size() <= 0) {
            setCurrentPage(defaultPage);
        } else {
            setCurrentPage(getPages().get(0));
        }
    }

    public IPage getCurrentPage() {
        return currentPage;
    }

    public List<IPage> getPages() {
        return pages;
    }

    private class PageSelectionProvider implements ISelectionProvider {

        private ISelection selection;

        private List<ISelectionChangedListener> selectionChangedListener = Lists.newCopyOnWriteArrayList();

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            selectionChangedListener.add(listener);
        }

        @Override
        public ISelection getSelection() {
            return selection;
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            selectionChangedListener.remove(listener);
        }

        @Override
        public void setSelection(ISelection selection) {
            this.selection = selection;
            final SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);
            for (final ISelectionChangedListener listener : selectionChangedListener) {
                SafeRunnable.run(new SafeRunnable() {

                    @Override
                    public void run() {
                        listener.selectionChanged(selectionChangedEvent);
                    }
                });
            }
        }
    }
}
