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

package org.eclipse.buildship.ui.view.task;

import java.util.Set;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelectionProvider;
import org.eclipse.buildship.ui.util.nodeselection.SelectionHistoryManager;

/**
 * A view displaying the Gradle tasks of the Gradle projects in the workspace.
 */
public final class TaskView extends ViewPart implements NodeSelectionProvider {

    // view id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.ui.views.taskview"; //$NON-NLS-1$

    private TaskViewState state;
    private UiContributionManager uiContributionManager;
    private QuickSearchManager quickSearchManager;
    private SelectionHistoryManager selectionHistoryManager;

    private PageBook pages;
    private Label emptyInputPage;
    private Label errorInputPage;
    private Composite nonEmptyInputPage;
    private TreeViewer treeViewer;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);

        // load the persisted state before we create any UI components that query for some state
        this.state = new TaskViewState();
        this.state.load();
    }

    @Override
    public void createPartControl(Composite parent) {
        // the top-level control changing its content depending on whether the content provider
        // contains task data to display or not
        this.pages = new PageBook(parent, SWT.NONE);

        // if there is no task data to display, show only a label
        this.emptyInputPage = new Label(this.pages, SWT.NONE);
        this.emptyInputPage.setText(TasksViewMessages.Label_No_Gradle_Projects);

        // if there is a problem loading the task data, show an error label
        this.errorInputPage = new Label(this.pages, SWT.NONE);
        this.errorInputPage.setText(TasksViewMessages.Label_Reload_Problem);
        this.errorInputPage.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

        // if there is task data to display, show the task tree and the search label on the bottom
        this.nonEmptyInputPage = new Composite(this.pages, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = gridLayout.marginHeight = gridLayout.verticalSpacing = 0;
        this.nonEmptyInputPage.setLayout(gridLayout);

        // add tree with two columns
        this.treeViewer = new TreeViewer(this.nonEmptyInputPage, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        this.treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        // set content provider and label provider on the tree
        this.treeViewer.setFilters(TaskNodeViewerFilter.createFor(this.state));
        this.treeViewer.setComparator(TaskNodeViewerSorter.createFor(this.state));
        this.treeViewer.setContentProvider(new TaskViewContentProvider(this, CorePlugin.modelRepositoryProvider(), CorePlugin.processStreamsProvider(), CorePlugin
                .workspaceOperations()));
        this.treeViewer.setLabelProvider(new TaskViewLabelProvider());

        // add columns to the tree
        final TreeColumn taskNameColumn = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
        taskNameColumn.setText(TasksViewMessages.Tree_Column_Name_Text);
        taskNameColumn.setWidth(this.state.getHeaderNameColumnWidth());

        final TreeColumn taskDescriptionColumn = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
        taskDescriptionColumn.setText(TasksViewMessages.Tree_Column_Description_Text);
        taskDescriptionColumn.setWidth(this.state.getHeaderDescriptionColumnWidth());

        // when changed save the header width into the state
        taskNameColumn.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                TaskView.this.state.setHeaderNameColumnWidth(taskNameColumn.getWidth());
            }
        });

        taskDescriptionColumn.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                TaskView.this.state.setHeaderDescriptionColumnWidth(taskDescriptionColumn.getWidth());
            }
        });

        // configure and manage a label with quick search functionality
        Label quickSearchLabel = new Label(this.nonEmptyInputPage, SWT.NONE);
        quickSearchLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
        this.quickSearchManager = new QuickSearchManager(this.treeViewer.getTree(), quickSearchLabel);

        // manage the selection history as required for the task execution
        getSite().setSelectionProvider(this.treeViewer);
        this.selectionHistoryManager = new SelectionHistoryManager(this.getTreeViewer());

        // create toolbar actions, menu items, event listeners, etc.
        this.uiContributionManager = new UiContributionManager(this);
        this.uiContributionManager.wire();

        // set initial content (use fetch strategy LOAD_IF_NOT_CACHED since
        // the model might already be available in case a project import has
        // just happened)
        reload(FetchStrategy.LOAD_IF_NOT_CACHED);
    }

    /**
     * Triggers a refresh of the view.
     */
    public void refresh() {
        this.treeViewer.refresh(true);

        // ensure the quick search is reset when the tree view is refreshed
        // in order to avoid a stale state of the selection history
        // (there seems to be no listener available to handle this more generically)
        this.quickSearchManager.reset();
    }

    /**
     * Triggers a reload of the content backing this view. In case the input is empty, the tree
     * viewer is hidden and only a label is visible which indicates that there are no tasks to be
     * displayed.
     */
    public void reload(FetchStrategy modelFetchStrategy) {
        try {
            Set<ProjectConfiguration> rootProjectConfigs = CorePlugin.projectConfigurationManager().getRootProjectConfigurations();
            this.pages.showPage(rootProjectConfigs.isEmpty() ? this.emptyInputPage : this.nonEmptyInputPage);
            this.treeViewer.setInput(new TaskViewContent(rootProjectConfigs, modelFetchStrategy));
        } catch (Exception e) {
            CorePlugin.logger().error("Failed to reload task view content.", e); //$NON-NLS-1$
            this.pages.showPage(this.errorInputPage);
        }
    }

    public void handleProjectAddition(IProject project) {
        reload(FetchStrategy.LOAD_IF_NOT_CACHED);
    }

    public void handleProjectRemoval(IProject project) {
        reload(FetchStrategy.LOAD_IF_NOT_CACHED);
    }

    @Override
    public void setFocus() {
        this.pages.setFocus();
    }

    public TaskViewState getState() {
        return this.state;
    }

    public TreeViewer getTreeViewer() {
        return this.treeViewer;
    }

    @Override
    public NodeSelection getSelection() {
        return this.selectionHistoryManager.getSelectionHistory();
    }

    @Override
    public void dispose() {
        this.state.dispose();
        this.quickSearchManager.dispose();
        this.selectionHistoryManager.dispose();
        this.uiContributionManager.dispose();
        super.dispose();
    }

}
