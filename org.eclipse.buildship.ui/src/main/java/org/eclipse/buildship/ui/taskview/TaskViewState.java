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

package org.eclipse.buildship.ui.taskview;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Represents the (persistable) configuration state of the {@link TaskView}. Backed by the Eclipse
 * Preferences API.
 */
public final class TaskViewState {

    private final String PREF_PROJECT_TASKS_VISIBLE = "taskview.projectTasksVisible";
    private final String PREF_TASK_SELECTORS_VISIBLE = "taskview.taskSelectorsVisible";
    private final String PREF_PRIVATE_TASKS_VISIBLE = "taskview.privateTasksVisible";
    private final String PREF_SORT_BY_TYPE = "taskView.sortByType";
    private final String PREF_SORT_BY_VISIBILITY = "taskView.sortByVisibility";
    private final String PREF_LINK_TO_SELECTION = "taskview.linkToSelection";
    private final String PREF_SHOW_TREE_HEADER = "taskView.showTreeHeader";

    private boolean projectTasksVisible;
    private boolean taskSelectorsVisible;
    private boolean privateTasksVisible;
    private boolean sortByType;
    private boolean sortByVisibility;
    private boolean linkToSelection;
    private boolean showTreeHeader;

    public void load() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        this.projectTasksVisible = prefs.getBoolean(this.PREF_PROJECT_TASKS_VISIBLE, true);
        this.taskSelectorsVisible = prefs.getBoolean(this.PREF_TASK_SELECTORS_VISIBLE, true);
        this.privateTasksVisible = prefs.getBoolean(this.PREF_PRIVATE_TASKS_VISIBLE, false);
        this.sortByType = prefs.getBoolean(this.PREF_SORT_BY_TYPE, true);
        this.sortByVisibility = prefs.getBoolean(this.PREF_SORT_BY_VISIBILITY, true);
        this.linkToSelection = prefs.getBoolean(this.PREF_LINK_TO_SELECTION, false);
        this.showTreeHeader = prefs.getBoolean(this.PREF_SHOW_TREE_HEADER, false);
    }

    public void save() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        prefs.putBoolean(this.PREF_PROJECT_TASKS_VISIBLE, this.projectTasksVisible);
        prefs.putBoolean(this.PREF_TASK_SELECTORS_VISIBLE, this.taskSelectorsVisible);
        prefs.putBoolean(this.PREF_PRIVATE_TASKS_VISIBLE, this.privateTasksVisible);
        prefs.putBoolean(this.PREF_SORT_BY_TYPE, this.sortByType);
        prefs.putBoolean(this.PREF_SORT_BY_VISIBILITY, this.sortByVisibility);
        prefs.putBoolean(this.PREF_LINK_TO_SELECTION, this.linkToSelection);
        prefs.putBoolean(this.PREF_SHOW_TREE_HEADER, this.showTreeHeader);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            UiPlugin.logger().error("Unable to store task view preferences.", e);
        }
    }

    public boolean isProjectTasksVisible() {
        return this.projectTasksVisible;
    }

    public void setProjectTasksVisible(boolean projectTasksVisible) {
        this.projectTasksVisible = projectTasksVisible;
    }

    public boolean isTaskSelectorsVisible() {
        return this.taskSelectorsVisible;
    }

    public void setTaskSelectorsVisible(boolean taskSelectorsVisible) {
        this.taskSelectorsVisible = taskSelectorsVisible;
    }

    public boolean isPrivateTasksVisible() {
        return this.privateTasksVisible;
    }

    public void setPrivateTasksVisible(boolean privateTasksVisible) {
        this.privateTasksVisible = privateTasksVisible;
    }

    public boolean isSortByType() {
        return this.sortByType;
    }

    public void setSortByType(boolean sortByType) {
        this.sortByType = sortByType;
    }

    public boolean isSortByVisibility() {
        return this.sortByVisibility;
    }

    public void setSortByVisibility(boolean sortByVisibility) {
        this.sortByVisibility = sortByVisibility;
    }

    public boolean isLinkToSelection() {
        return this.linkToSelection;
    }

    public void setLinkToSelection(boolean linkToSelection) {
        this.linkToSelection = linkToSelection;
    }

    public boolean isShowTreeHeader() {
        return this.showTreeHeader;
    }

    public void setShowTreeHeader(boolean showTreeHeader) {
        this.showTreeHeader = showTreeHeader;
    }

    public void dispose() {
        save();
    }

}
