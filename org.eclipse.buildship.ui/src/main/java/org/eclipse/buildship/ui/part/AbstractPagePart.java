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

package org.eclipse.buildship.ui.part;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.util.LinkedList;
import java.util.List;

/**
 * This abstract part manages different {@link IPage} elements, which can be shown on this part.
 */
public abstract class AbstractPagePart extends ViewPart {

    private IPage currentPage;
    private List<IPage> pages = new LinkedList<IPage>();

    private PageSelectionProvider pageSelectionProvider;

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            AbstractPagePart.this.pageSelectionProvider.setSelection(event.getSelection());
        }
    };

    private Composite stackComposite;
    private StackLayout stackLayout;
    private IPage defaultPage;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // install a custom selection provider
        this.pageSelectionProvider = new PageSelectionProvider();
        site.setSelectionProvider(this.pageSelectionProvider);
    }

    @Override
    public final void createPartControl(Composite parent) {
        this.stackComposite = new Composite(parent, SWT.NONE);
        this.stackLayout = new StackLayout();
        this.stackComposite.setLayout(this.stackLayout);
        this.defaultPage = getDefaultPage();
        this.defaultPage.createPage(this.stackComposite);
        setCurrentPage(this.defaultPage);
    }

    protected abstract IPage getDefaultPage();

    public void addPage(IPage page) {
        page.createPage(this.stackComposite);
        this.pages.add(page);
    }

    @Override
    public void setFocus() {
        this.currentPage.setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void switchToNextPage() {
        if (!this.pages.isEmpty()) {
            IPage currentPage = this.currentPage;
            int nextPage = (this.pages.indexOf(currentPage) + 1) % this.pages.size();
            switchToPageAtIndex(nextPage);
        }
    }

    public void switchToPageAtIndex(int index) {
        Preconditions.checkArgument(index >= 0 && index < this.pages.size());
        IPage page = this.pages.get(index);
        setCurrentPage(page);
    }

    public void setCurrentPage(IPage page) {
        Control pageControl = page.getPageControl();
        if (pageControl != null && !pageControl.getParent().equals(this.stackComposite)) {
            throw new RuntimeException("The given page does not belong to this PagePart.");
        }
        // remove previous selectionprovider
        changeInternalSelectionProvider(this.currentPage, true);

        this.currentPage = page;
        this.stackLayout.topControl = pageControl;
        this.stackComposite.layout();

        // always set the currently shown page as selectionprovider
        changeInternalSelectionProvider(page, false);
    }

    private void changeInternalSelectionProvider(IPage page, boolean remove) {
        if (page instanceof SelectionProviderProvider) {
            SelectionProviderProvider selectionProviderProvider = (SelectionProviderProvider) page;
            if (selectionProviderProvider.getSelectionProvider() != null) {
                if (remove) {
                    selectionProviderProvider.getSelectionProvider().removeSelectionChangedListener(this.selectionChangedListener);
                } else {
                    selectionProviderProvider.getSelectionProvider().addSelectionChangedListener(this.selectionChangedListener);
                    this.pageSelectionProvider.setSelection(selectionProviderProvider.getSelectionProvider().getSelection());
                }
            }
        }
    }

    public void removeAllPages() {
        for (IPage page : getPages()) {
            page.dispose();
        }

        getPages().clear();
        setCurrentPage(this.defaultPage);
    }

    public void removeCurrentPage() {
        if (this.currentPage != this.defaultPage) {
            int index = this.pages.indexOf(this.currentPage);

            this.currentPage.dispose();
            this.pages.remove(this.currentPage);

            if (!this.pages.isEmpty()) {
                switchToPageAtIndex(Math.min(index, this.pages.size() - 1));
            } else {
                setCurrentPage(this.defaultPage);
            }
        }
    }

    public IPage getCurrentPage() {
        return this.currentPage != this.defaultPage ? this.currentPage : null;
    }

    public List<IPage> getPages() {
        return this.pages;
    }

    public boolean hasPages() {
        return !this.pages.isEmpty();
    }

    public int getPageCount() {
        return this.pages.size();
    }

    /**
     * Offer a common ISelectionProvider the currently active page, which is shown on the
     * {@link AbstractPagePart}.
     */
    private class PageSelectionProvider implements ISelectionProvider {

        private ISelection selection;

        private List<ISelectionChangedListener> selectionChangedListener = Lists.newCopyOnWriteArrayList();

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            this.selectionChangedListener.add(listener);
        }

        @Override
        public ISelection getSelection() {
            return this.selection;
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            this.selectionChangedListener.remove(listener);
        }

        @Override
        public void setSelection(ISelection selection) {
            this.selection = selection;
            final SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);
            for (final ISelectionChangedListener listener : this.selectionChangedListener) {
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
