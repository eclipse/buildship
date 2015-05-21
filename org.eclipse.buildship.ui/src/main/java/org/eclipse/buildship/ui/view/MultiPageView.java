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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.GradleException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.part.ViewPart;

/**
 * This abstract part manages different {@link Page} elements, which can be shown on this part. If
 * your {@link Page} implementation contains an {@link ISelectionProvider} it should return it as
 * adapter in the {@link #getAdapter(Class)} method.
 *
 * @see MessagePage
 * @see org.eclipse.buildship.ui.view.execution.ExecutionPage
 */
public abstract class MultiPageView extends ViewPart {

    private Page currentPage;
    private List<Page> pages = new LinkedList<Page>();
    private PageSelectionProvider pageSelectionProvider;
    private Composite stackComposite;
    private StackLayout stackLayout;
    private Page defaultPage;
    private IContributionItem switchPagesAction;

    private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            MultiPageView.this.pageSelectionProvider.setSelection(event.getSelection());
        }
    };

    private IPropertyChangeListener actionBarPropertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS) && getCurrentPage().getSite() != null
                    && event.getSource() == getCurrentPage().getSite().getActionBars()) {
                refreshGlobalActionHandlers();
            }
        }
    };

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // install a custom selection provider
        this.pageSelectionProvider = new PageSelectionProvider();
        site.setSelectionProvider(this.pageSelectionProvider);

        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        this.switchPagesAction = new ActionContributionItem(new SwitchToNextPageAction(this));
        toolBarManager.add(this.switchPagesAction);

        checkGlobalActionVisibility();
    }

    protected abstract Page getDefaultPage();

    @Override
    public final void createPartControl(Composite parent) {
        this.stackComposite = new Composite(parent, SWT.NONE);
        this.stackLayout = new StackLayout();
        this.stackComposite.setLayout(this.stackLayout);
        this.defaultPage = getDefaultPage();
        this.defaultPage.createPage(this.stackComposite);
        initPage(this.defaultPage);
        setCurrentPage(this.defaultPage);
    }

    public void addPage(Page page) {
        page.createPage(this.stackComposite);
        initPage(page);
        this.pages.add(page);
        checkGlobalActionVisibility();
    }

    private void initPage(Page page) {
        DefaultPageSite pageSite = new DefaultPageSite(getViewSite());
        SubActionBars actionBars = (SubActionBars) pageSite.getActionBars();
        actionBars.addPropertyChangeListener(this.actionBarPropertyChangeListener);
        page.init(pageSite);
    }

    @Override
    public void setFocus() {
        if (getCurrentPage() != null) {
            getCurrentPage().setFocus();
        } else {
            this.stackComposite.setFocus();
        }
    }

    public void switchToNextPage() {
        if (!this.pages.isEmpty()) {
            Page currentPage = this.currentPage;
            int nextPage = (this.pages.indexOf(currentPage) + 1) % this.pages.size();
            switchToPageAtIndex(nextPage);
        }
    }

    public void switchToPageAtIndex(int index) {
        Preconditions.checkArgument(index >= 0 && index < this.pages.size());
        Page page = this.pages.get(index);
        setCurrentPage(page);
    }

    public void setCurrentPage(Page page) {
        Control pageControl = page.getPageControl();
        if (pageControl != null && !pageControl.getParent().equals(this.stackComposite) && !getPages().contains(page)) {
            throw new GradleException("The given page does not belong to this PagePart.");
        }
        if (this.currentPage != null) {
            // remove previous selectionprovider
            changeInternalSelectionProvider(this.currentPage, true);

            // deactivate previous page's ActionBar
            SubActionBars actionBars = (SubActionBars) this.currentPage.getSite().getActionBars();
            actionBars.deactivate();
        }

        this.currentPage = page;
        this.stackLayout.topControl = pageControl;
        this.stackComposite.layout();

        // always set the currently shown page as ISelectionProvider
        changeInternalSelectionProvider(page, false);

        // activate page's ActionBar
        SubActionBars actionBars = (SubActionBars) page.getSite().getActionBars();
        actionBars.activate();

        // update the complete view's ActionBar
        getViewSite().getActionBars().updateActionBars();
    }

    private void changeInternalSelectionProvider(Page page, boolean remove) {

        @SuppressWarnings("cast")
        ISelectionProvider selectionProvider = (ISelectionProvider) page.getAdapter(ISelectionProvider.class);

        if (selectionProvider != null) {
            if (remove) {
                selectionProvider.removeSelectionChangedListener(this.selectionChangedListener);
            } else {
                selectionProvider.addSelectionChangedListener(this.selectionChangedListener);
                this.pageSelectionProvider.setSelection(selectionProvider.getSelection());
            }
        }
    }

    private void checkGlobalActionVisibility() {
        this.switchPagesAction.setVisible(hasPages());
    }

    public void removeAllPages() {
        for (Page page : getPages()) {
            page.getSite().dispose();
            page.dispose();
        }

        getPages().clear();
        checkGlobalActionVisibility();
        setCurrentPage(this.defaultPage);
    }

    public void removeCurrentPage() {
        if (this.currentPage != this.defaultPage) {
            int index = this.pages.indexOf(this.currentPage);

            this.currentPage.getSite().dispose();
            this.currentPage.dispose();
            this.pages.remove(this.currentPage);

            if (hasPages()) {
                switchToPageAtIndex(Math.min(index, this.pages.size() - 1));
            } else {
                setCurrentPage(this.defaultPage);
                checkGlobalActionVisibility();
            }
        }
    }

    public Page getCurrentPage() {
        return this.currentPage != this.defaultPage ? this.currentPage : null;
    }

    public List<Page> getPages() {
        return this.pages;
    }

    public boolean hasPages() {
        return !this.pages.isEmpty();
    }

    @Override
    public void dispose() {
        for (Page page : getPages()) {
            page.getSite().dispose();
            page.dispose();
        }
        super.dispose();
    }

    /**
     * Refreshes the global actions for the active page.
     */
    private void refreshGlobalActionHandlers() {
        // Clear old actions.
        IActionBars bars = getViewSite().getActionBars();
        bars.clearGlobalActionHandlers();

        // Set new actions.
        Map<?, ?> newActionHandlers = ((SubActionBars) getCurrentPage().getSite().getActionBars()).getGlobalActionHandlers();
        if (newActionHandlers != null) {
            Set<?> keys = newActionHandlers.entrySet();
            Iterator<?> iter = keys.iterator();
            while (iter.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
                bars.setGlobalActionHandler((String) entry.getKey(), (IAction) entry.getValue());
            }
        }
    }

    /**
     * Implementation of IPageSite, which creates a {@link SubActionBars} for the
     * {@link IActionBars} of a given {@link IViewSite}.
     *
     */
    private static final class DefaultPageSite implements PageSite {

        private final IViewSite viewSite;
        private final SubActionBars subActionBars;

        public DefaultPageSite(IViewSite viewSite) {
            this.viewSite = viewSite;
            this.subActionBars = new SubActionBars(viewSite.getActionBars());
        }

        @Override
        public IActionBars getActionBars() {
            return this.subActionBars;
        }

        @Override
        public IViewSite getViewSite() {
            return this.viewSite;
        }

        @Override
        public void dispose() {
            if (this.subActionBars != null) {
                this.subActionBars.dispose();
            }
        }

    }

    /**
     * Offer a common ISelectionProvider the currently active page, which is shown on the
     * {@link MultiPageView}.
     */
    private static final class PageSelectionProvider implements ISelectionProvider {

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
