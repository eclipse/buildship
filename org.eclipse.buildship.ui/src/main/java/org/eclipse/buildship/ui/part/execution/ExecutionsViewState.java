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

package org.eclipse.buildship.ui.part.execution;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.part.TreeViewerState;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Represents the (persistable) configuration state of the {@link ExecutionsView}. Backed by the
 * Eclipse Preferences API.
 */
public final class ExecutionsViewState implements TreeViewerState {

    private static final String PREF_SHOW_TREE_HEADER = "executionsView.showTreeHeader";

    private boolean showTreeHeader;

    public void load() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        this.showTreeHeader = prefs.getBoolean(PREF_SHOW_TREE_HEADER, false);
    }

    public void save() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        prefs.putBoolean(PREF_SHOW_TREE_HEADER, this.showTreeHeader);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            UiPlugin.logger().error("Unable to store execution view preferences.", e);
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

    public void dispose() {
        save();
    }

}
