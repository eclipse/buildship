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

package org.eclipse.buildship.ui.internal.view;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Abstract view part that manages multiple {@link Page} elements shown within this view part. If
 * your {@link Page} implementation contains an {@link ISelectionProvider} it should return it as
 * an adapter through the {@link #getAdapter(Class)} method.
 *
 * @see MessagePage
 * @see Page
 */
public abstract class MultiPageView extends ViewPart {

    /**
     * The name of the group of page-specific actions in the toolbar of the {@code MultiPageView}.
     */
    public static final String PAGE_GROUP = "pageGroup"; //$NON-NLS-1$

    /**
     * The name of the group of global actions in the toolbar of the {@code MultiPageView}.
     */
    public static final String PART_GROUP = "partGroup"; //$NON-NLS-1$

    private final List<Page> pages; // contains all pages except the default page
    private Page currentPage;
    private Page defaultPage;

    private final PageSelectionProvider pageSelectionProvider;
    private final ISelectionChangedListener selectionChangedListener;
    private final IPropertyChangeListener actionBarPropertyChangeListener;

    private Composite stackComposite;
    private StackLayout stackLayout;

    protected MultiPageView() {
        this.pages = new LinkedList<Page>();
        this.currentPage = null;
        this.defaultPage = null;

        this.pageSelectionProvider = new PageSelectionProvider();
        this.selectionChangedListener = new ForwardingSelectionChangedListener(this.pageSelectionProvider);
        this.actionBarPropertyChangeListener = new ActionBarsPropertyChangeListener(this);
    }

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // install a custom selection provider
        site.setSelectionProvider(this.pageSelectionProvider);

        // add the global actions to the view's toolbar,
        // incl. separators to position page specific actions more accurately
        IToolBarManager toolBarManager = site.getActionBars().getToolBarManager();
        toolBarManager.add(new Separator(PAGE_GROUP));
        toolBarManager.add(new Separator(PART_GROUP));
    }

    @Override
    public final void createPartControl(Composite parent) {
        // create a stack for the pages
        this.stackComposite = new Composite(parent, SWT.NONE);
        this.stackLayout = new StackLayout();
        this.stackComposite.setLayout(this.stackLayout);

        // add the default page to the stack (but do not add it to the list of pages)
        this.defaultPage = createDefaultPage();
        this.defaultPage.createPage(this.stackComposite);
        initPage(this.defaultPage);

        // initially show the default page since there are no other pages
        switchToPage(this.defaultPage);
        updateVisibilityOfGlobalActions();
    }

    protected abstract Page createDefaultPage();

    public void addPage(Page page) {
        int index = this.pages.indexOf(page);
        if (index != -1) {
            throw new GradlePluginsRuntimeException("Page already known: " + page.getDisplayName());
        }

        // add the given page
        page.createPage(this.stackComposite);
        initPage(page);
        this.pages.add(page);
        updateVisibilityOfGlobalActions();
    }

    private void initPage(Page page) {
        DefaultPageSite pageSite = new DefaultPageSite(getViewSite());
        SubActionBars actionBars = (SubActionBars) pageSite.getActionBars();
        actionBars.addPropertyChangeListener(this.actionBarPropertyChangeListener);
        page.init(pageSite);
    }

    public void removePage(Page page) {
        int index = this.pages.indexOf(page);
        if (index == -1) {
            throw new GradlePluginsRuntimeException("Unknown page: " + page.getDisplayName());
        }

        // dispose and remove the given page
        page.getSite().dispose();
        page.dispose();
        this.pages.remove(index);
        updateVisibilityOfGlobalActions();

        // show another page
        if (hasPages()) {
            // show the adjacent page to the right
            switchToPageAtIndex(Math.min(index, this.pages.size() - 1));
        } else {
            // show the default page
            switchToPage(this.defaultPage);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void removeAllPages() {
        for (Iterator<Page> iterator = this.pages.iterator(); iterator.hasNext(); ) {
            Page page = iterator.next();
            page.getSite().dispose();
            page.dispose();
            iterator.remove();
        }
        updateVisibilityOfGlobalActions();

        // show the default page
        switchToPage(this.defaultPage);
    }

    public void switchToNextPage() {
        if (hasPages()) {
            int nextPage = (this.pages.indexOf(getCurrentPage()) + 1) % this.pages.size();
            switchToPageAtIndex(nextPage);
        }
    }

    public void switchToPageAtIndex(int index) {
        if (index < 0 && index >= this.pages.size()) {
            throw new GradlePluginsRuntimeException("Page index out of bounds: " + index);
        }

        Page page = this.pages.get(index);
        switchToPage(page);
    }

    public void switchToPage(Page page) {
        int index = this.pages.indexOf(page);
        if (index == -1 && page != this.defaultPage) {
            throw new GradlePluginsRuntimeException("Unknown page: " + page.getDisplayName());
        }

        if (this.currentPage != null) {
            // remove previous selection provider
            removeSelectionListenerThatUpdatesSelectionProvider(this.currentPage);

            // deactivate previous page's ActionBar
            SubActionBars actionBars = (SubActionBars) this.currentPage.getSite().getActionBars();
            actionBars.deactivate();
        }

        this.currentPage = page;
        this.stackLayout.topControl = page.getPageControl();
        this.stackComposite.layout();

        // always set the currently shown page as the selection provider
        addSelectionListenerThatUpdatesSelectionProvider(page);

        // activate page's ActionBar
        SubActionBars actionBars = (SubActionBars) page.getSite().getActionBars();
        actionBars.activate();

        // update the complete view's ActionBar
        getViewSite().getActionBars().updateActionBars();
    }

    private void addSelectionListenerThatUpdatesSelectionProvider(Page page) {
        @SuppressWarnings({"cast", "RedundantCast"})
        ISelectionProvider selectionProvider = (ISelectionProvider) page.getAdapter(ISelectionProvider.class);
        if (selectionProvider != null) {
            selectionProvider.addSelectionChangedListener(this.selectionChangedListener);
            this.pageSelectionProvider.setSelection(selectionProvider.getSelection());
        }

    }

    private void removeSelectionListenerThatUpdatesSelectionProvider(Page page) {
        @SuppressWarnings({"cast", "RedundantCast"})
        ISelectionProvider selectionProvider = (ISelectionProvider) page.getAdapter(ISelectionProvider.class);
        if (selectionProvider != null) {
            selectionProvider.removeSelectionChangedListener(this.selectionChangedListener);
        }
    }

    public Page getCurrentPage() {
        return this.currentPage != this.defaultPage ? this.currentPage : null;
    }

    public List<Page> getPages() {
        return ImmutableList.copyOf(this.pages);
    }

    public boolean hasPages() {
        return !this.pages.isEmpty();
    }

    protected void updateVisibilityOfGlobalActions(){}

    private void refreshGlobalActionHandlers() {
        // clear old action handlers
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.clearGlobalActionHandlers();

        // set new action handlers
        Map<?, ?> newActionHandlers = ((SubActionBars) getCurrentPage().getSite().getActionBars()).getGlobalActionHandlers();
        if (newActionHandlers != null) {
            for (Map.Entry<?, ?> entry : newActionHandlers.entrySet()) {
                actionBars.setGlobalActionHandler((String) entry.getKey(), (IAction) entry.getValue());
            }
        }
    }

    @Override
    public void setFocus() {
        Page currentPage = getCurrentPage();
        if (currentPage != null) {
            currentPage.setFocus();
        } else {
            this.defaultPage.setFocus();
        }
    }

    @Override
    public void dispose() {
        for (Iterator<Page> iterator = this.pages.iterator(); iterator.hasNext(); ) {
            Page page = iterator.next();
            page.getSite().dispose();
            page.dispose();
            iterator.remove();
        }
        super.dispose();
    }

    /**
     * Implementation of {@code PageSite}.
     */
    private static final class DefaultPageSite implements PageSite {

        private final IViewSite viewSite;
        private final SubActionBars subActionBars;

        private DefaultPageSite(IViewSite viewSite) {
            this.viewSite = Preconditions.checkNotNull(viewSite);
            this.subActionBars = new SubActionBars(viewSite.getActionBars());
        }

        @Override
        public IViewSite getViewSite() {
            return this.viewSite;
        }

        @Override
        public IActionBars getActionBars() {
            return this.subActionBars;
        }

        @Override
        public void dispose() {
            this.subActionBars.dispose();
        }

    }

    /**
     * Implementation of {@code ISelectionProvider}.
     */
    private static final class PageSelectionProvider implements ISelectionProvider {

        private final List<ISelectionChangedListener> selectionChangedListeners;
        private ISelection selection;

        private PageSelectionProvider() {
            this.selectionChangedListeners = Lists.newCopyOnWriteArrayList();
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            this.selectionChangedListeners.add(listener);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            this.selectionChangedListeners.remove(listener);
        }

        @Override
        public ISelection getSelection() {
            return this.selection;
        }

        @Override
        public void setSelection(ISelection selection) {
            this.selection = selection;
            final SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);
            for (final ISelectionChangedListener listener : this.selectionChangedListeners) {
                SafeRunnable.run(new SafeRunnable() {

                    @Override
                    public void run() {
                        listener.selectionChanged(selectionChangedEvent);
                    }
                });
            }
        }

    }

    /**
     * Implementation of {@code ISelectionChangedListener}.
     */
    private static final class ForwardingSelectionChangedListener implements ISelectionChangedListener {

        private final ISelectionProvider selectionProvider;

        private ForwardingSelectionChangedListener(ISelectionProvider selectionProvider) {
            this.selectionProvider = Preconditions.checkNotNull(selectionProvider);
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            this.selectionProvider.setSelection(event.getSelection());
        }

    }

    /**
     * Implementation of {@code IPropertyChangeListener}.
     */
    private static final class ActionBarsPropertyChangeListener implements IPropertyChangeListener {

        private final MultiPageView multiPageView;

        private ActionBarsPropertyChangeListener(MultiPageView multiPageView) {
            this.multiPageView = Preconditions.checkNotNull(multiPageView);
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (SubActionBars.P_ACTION_HANDLERS.equals(event.getProperty())) {
                Page currentPage = this.multiPageView.getCurrentPage();
                if (currentPage != null && currentPage.getSite() != null && currentPage.getSite().getActionBars() == event.getSource()) {
                    this.multiPageView.refreshGlobalActionHandlers();
                }
            }
        }

    }

}
