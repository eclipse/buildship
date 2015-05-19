/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.view.execution.ExecutionsViewMessages;

/**
 * Base class for views supporting multiple pages.
 * <p/>
 * Clients defining a view should extend this class. The {@link MultiPageManager} instance returned
 * by the {@link #getManager()} should be used to add or remove a page and to contribute to the
 * toolbars of each page.
 */
public abstract class MultiPageView extends PageBookView {

    /**
     * By default the PageBookView superclass checks for IWorkbenchPart as input. To support custom
     * content this this dummy implementation is passed to the view.
     *
     * @see MultiPageView#isImportant(IWorkbenchPart)
     */
    private static class MultiPageViewInputPart implements IWorkbenchPart {

        private final IWorkbenchPartSite site;
        private final IPageBookViewPage page;

        public MultiPageViewInputPart(IWorkbenchPartSite site, IPageBookViewPage page) {
            this.site = site;
            this.page = page;
        }

        @Override
        public IWorkbenchPartSite getSite() {
            return this.site;
        }

        public IPageBookViewPage getPage() {
            return this.page;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public void addPropertyListener(IPropertyListener listener) {
        }

        @Override
        public void createPartControl(Composite parent) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public Image getTitleImage() {
            return null;
        }

        @Override
        public String getTitleToolTip() {
            return null;
        }

        @Override
        public void removePropertyListener(IPropertyListener listener) {
        }

        @Override
        public void setFocus() {
        }

    }

    /**
     * Simple implementation of the {@link MultiPageManager} interface.
     */
    final class PageViewManager implements MultiPageManager {

        @Override
        public void registerPageParticipantFactory(PageParticipantFactory participant) {
            MultiPageView.this.registerPageParticipant(participant);
        }

        @Override
        public void addPage(IPageBookViewPage page) {
            MultiPageView.this.addPage(page);
        }

        @Override
        public void removePage(IPageBookViewPage page) {
            MultiPageView.this.removePage(page);
        }

        @Override
        public IPageBookViewPage currentPage() {
            return MultiPageView.this.currentPage();
        }

        @Override
        public List<IPageBookViewPage> allPages() {
            return MultiPageView.this.allPages();
        }

    }

    private final MultiPageManager manager;
    private final Object LOCK = new Object();
    private final Map<IPageBookViewPage, MultiPageViewInputPart> pageToInputPart;
    private final List<PageParticipantFactory> participantFactories;
    private final Map<IPageBookViewPage, List<PageParticipant>> participants;
    private final Stack<IPageBookViewPage> pages;

    private IPageBookViewPage currentPage;

    public MultiPageView() {
        super();
        this.manager = new PageViewManager();
        this.participantFactories = new ArrayList<PageParticipantFactory>();
        this.participants = new HashMap<IPageBookViewPage, List<PageParticipant>>();
        this.pageToInputPart = new HashMap<IPageBookViewPage, MultiPageViewInputPart>();
        this.pages = new Stack<IPageBookViewPage>();
    }

    public void registerPageParticipant(PageParticipantFactory participantFactory) {
        synchronized (this.LOCK) {
            this.participantFactories.add(participantFactory);
        }
    }

    public MultiPageManager getManager() {
        return this.manager;
    }

    private void addPage(IPageBookViewPage page) {
        MultiPageViewInputPart inputPart = new MultiPageViewInputPart(getViewSite(), page);
        this.pageToInputPart.put(page, inputPart);
        this.pages.add(page);
        partActivated(inputPart);
    }

    private void removePage(IPageBookViewPage page) {
        MultiPageViewInputPart inputPart = this.pageToInputPart.get(page);
        if (inputPart != null) {
            this.pages.remove(page);
            partClosed(inputPart);

            // open the previous page if any
            if (!this.pages.isEmpty()) {
                MultiPageViewInputPart previousInputPart = this.pageToInputPart.get(this.pages.peek());
                if (previousInputPart != null) {
                    partActivated(previousInputPart);
                }
            }
        }
    }

    private IPageBookViewPage currentPage() {
        return this.currentPage;
    }

    private List<IPageBookViewPage> allPages() {
        return ImmutableList.copyOf(this.pages);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }

    @Override
    protected IPage createDefaultPage(PageBook book) {
        // TODO (donat) move message to subclass
        MessagePage messagePage = new MessagePage();
        messagePage.setMessage(ExecutionsViewMessages.Label_No_Execution);
        messagePage.createControl(getPageBook());
        initPage(messagePage);
        return messagePage;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        MultiPageViewInputPart inputPart = (MultiPageViewInputPart) part;
        IPageBookViewPage page = inputPart.getPage();
        if (page instanceof IPageBookViewPage) {
            IPageBookViewPage viewPage = (IPageBookViewPage) page;
            initPage(viewPage);
            viewPage.createControl(getPageBook());
            participantsInit(viewPage.getSite(), page);
            PageRec result = new PageRec(part, viewPage);
            return result;
        } else {
            throw new GradlePluginsRuntimeException("no");
        }
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        MultiPageViewInputPart pagePart = (MultiPageViewInputPart) part;

        // dispose the participants
        participantsDispose(pagePart.page);

        // dispose IPageBookViewPage
        pagePart.dispose();

        // dispose page record
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    private void participantsInit(IPageSite site, IPageBookViewPage page) {

        // create new participant instances
        List<PageParticipantFactory> participantFactories = null;
        synchronized (this.LOCK) {
            participantFactories = ImmutableList.copyOf(this.participantFactories);
        }
        List<PageParticipant> participants = new ArrayList<PageParticipant>(participantFactories.size());
        for (PageParticipantFactory factory : participantFactories) {
            participants.add(factory.newParticipant());
        }

        // associate the new participants with the page being initialized
        synchronized (this.LOCK) {
            this.participants.put(page, ImmutableList.copyOf(participants));
        }

        // initialize the new page
        for (PageParticipant participant : participants) {
            participant.init(site, page);
        }
    }

    private void participantsDispose(IPageBookViewPage page) {
        Map<IPageBookViewPage, List<PageParticipant>> participantsMap = null;
        synchronized (this.LOCK) {
            participantsMap = ImmutableMap.copyOf(this.participants);
        }
        List<PageParticipant> participants = participantsMap.get(page);
        if (participants != null) {
            for (PageParticipant participant : participants) {
                participant.dispose();
            }
        }
    }

    @Override
    protected void showPageRec(PageRec pageRec) {
        IPage page = pageRec.page;
        if (page != null && page instanceof IPageBookViewPage) {
            this.currentPage = (IPageBookViewPage) page;
        }
        else {
            throw new GradlePluginsRuntimeException("Non-null IPageBookView instance expected, found " + page);
        }
        super.showPageRec(pageRec);
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        return part instanceof MultiPageViewInputPart;
    }

}
