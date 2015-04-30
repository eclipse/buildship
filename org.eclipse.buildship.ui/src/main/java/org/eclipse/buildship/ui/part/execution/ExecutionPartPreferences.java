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

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.buildship.ui.UiPlugin;

/**
 * Represents the (persistable) configuration state of the {@link ExecutionPart}. Backed by the
 * Eclipse Preferences API.
 */
public class ExecutionPartPreferences {

    public static final String PREF_SHOW_TREE_HEADER = "executionPart.showTreeHeader";
    public static final String PREF_SHOW_TREE_FILTER = "executionPart.showTreeFilter";

    private IEclipsePreferences prefs;

    public ExecutionPartPreferences() {
        @SuppressWarnings("deprecation")
        IEclipsePreferences preferences = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        // this is done to only have local @SuppressWarnings and may be removed, when Eclipse 3.6
        // will not be supported any more
        this.prefs = preferences;
    }

    public boolean getHeaderVisibile() {
        return prefs.getBoolean(PREF_SHOW_TREE_HEADER, false);
    }

    public void setHeaderVisibile(boolean treeHeaderVisible) {
        prefs.putBoolean(PREF_SHOW_TREE_HEADER, treeHeaderVisible);
        storePreferences();
    }

    public boolean getFilterVisibile() {
        return prefs.getBoolean(PREF_SHOW_TREE_FILTER, false);
    }

    public void setFilterVisibile(boolean treeFilterVisible) {
        prefs.putBoolean(PREF_SHOW_TREE_FILTER, treeFilterVisible);
        storePreferences();
    }

    private void storePreferences() {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            UiPlugin.logger().error("Unable to store execution part preferences.", e);
        }
    }

}
