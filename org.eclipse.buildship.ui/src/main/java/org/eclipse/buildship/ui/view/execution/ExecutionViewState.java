/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.ui.view.execution;

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.buildship.core.util.prefereces.PreferencesUtils;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.view.TreeViewerState;

/**
 * Represents the (persistable) configuration state of the {@link ExecutionsView}. Backed by the
 * Eclipse Preferences API.
 */
public final class ExecutionViewState implements TreeViewerState {

    private static final String PREF_SHOW_TREE_HEADER = "executionsView.showTreeHeader"; //$NON-NLS-1$
    private static final String PREF_HEADER_NAME_COLUMN_WIDTH = "executionsView.headerNameColumnWidth"; //$NON-NLS-1$
    private static final String PREF_HEADER_DURATION_COLUMN_WIDTH = "executionsView.headerDurationColumnWidth"; //$NON-NLS-1$

    private boolean showTreeHeader;
    private int headerNameColumnWidth;
    private int headerDurationColumnWidth;

    public void load() {
        IEclipsePreferences prefs = PreferencesUtils.getInstanceScope().getNode(UiPlugin.PLUGIN_ID);
        this.showTreeHeader = prefs.getBoolean(PREF_SHOW_TREE_HEADER, false);
        this.headerNameColumnWidth = prefs.getInt(PREF_HEADER_NAME_COLUMN_WIDTH, 600);
        this.headerDurationColumnWidth = prefs.getInt(PREF_HEADER_DURATION_COLUMN_WIDTH, 100);
    }

    public void save() {
        IEclipsePreferences prefs = PreferencesUtils.getInstanceScope().getNode(UiPlugin.PLUGIN_ID);
        prefs.putBoolean(PREF_SHOW_TREE_HEADER, this.showTreeHeader);
        prefs.putInt(PREF_HEADER_NAME_COLUMN_WIDTH, this.headerNameColumnWidth);
        prefs.putInt(PREF_HEADER_DURATION_COLUMN_WIDTH, this.headerDurationColumnWidth);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            UiPlugin.logger().error("Unable to store execution view preferences.", e); //$NON-NLS-1$
        }
    }

    @Override
    public boolean isShowTreeHeader() {
        return this.showTreeHeader;
    }

    @Override
    public void setShowTreeHeader(boolean showTreeHeader) {
        this.showTreeHeader = showTreeHeader;
    }

    public int getHeaderNameColumnWidth() {
        return this.headerNameColumnWidth;
    }

    public void setHeaderNameColumnWidth(int headerNameColumnWidth) {
        this.headerNameColumnWidth = headerNameColumnWidth;
    }

    public int getHeaderDurationColumnWidth() {
        return this.headerDurationColumnWidth;
    }

    public void setHeaderDurationColumnWidth(int headerDurationColumnWidth) {
        this.headerDurationColumnWidth = headerDurationColumnWidth;
    }

    public void dispose() {
        save();
    }

}
