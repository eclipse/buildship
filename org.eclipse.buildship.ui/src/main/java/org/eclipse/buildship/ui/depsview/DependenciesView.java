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

package org.eclipse.buildship.ui.depsview;

import java.util.Set;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.ui.taskview.TaskViewContent;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;

/**
 * A view displaying dependencies of Gradle projects in the workspace.
 */
public final class DependenciesView extends ViewPart {

    // view id declared in the plugin.xml
    public static final String ID = "org.eclipse.buildship.ui.views.dependenciesview"; //$NON-NLS-1$

    private UiContributionManager uiContributionManager;
    private DependenciesViewState state;

    private PageBook pages;
    private Label emptyInputPage;
    private Label errorInputPage;
    private Composite nonEmptyInputPage;
    private TreeViewer treeViewer;

    @Override
    public void createPartControl(Composite parent) {
        // load the persisted state before we create any UI components that query for some state
        this.state = new DependenciesViewState();
        this.state.load();

        // the top-level control changing its content depending on whether the content provider
        // contains task data to display or not
        this.pages = new PageBook(parent, SWT.NONE);

        // if there is no task data to display, show only a label
        this.emptyInputPage = new Label(this.pages, SWT.NONE);
        this.emptyInputPage.setText(DependencyViewMessages.Label_No_Gradle_Projects);

        // if there is a problem loading the task data, show an error label
        this.errorInputPage = new Label(this.pages, SWT.NONE);
        this.errorInputPage.setText(DependencyViewMessages.Label_Reload_Problem);
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
        this.treeViewer.setContentProvider(new DependenciesViewContentProvider(this, CorePlugin.modelRepositoryProvider(), CorePlugin.processStreamsProvider(), CorePlugin
                .workspaceOperations()));
        this.treeViewer.setLabelProvider(new DependenciesViewLabelProvider());

        // add columns to the tree
        TreeColumn column1 = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
        column1.setWidth(200);
        TreeColumn column2 = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
        column2.setWidth(200);
        TreeColumn column3 = new TreeColumn(this.treeViewer.getTree(), SWT.LEFT);
        column3.setWidth(200);

        // expose selection for the workbench UI to have the nodes visible in the properties view
        getSite().setSelectionProvider(this.treeViewer);

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

    // TODO (donat) WorspaceProjectsChangeListener used to call this method
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

    public TreeViewer getTreeViewer() {
        return this.treeViewer;
    }

    public DependenciesViewState getState() {
        return this.state;
    }

    @Override
    public void dispose() {
        this.state.dispose();
        this.uiContributionManager.dispose();
        super.dispose();
        super.dispose();
    }

}
