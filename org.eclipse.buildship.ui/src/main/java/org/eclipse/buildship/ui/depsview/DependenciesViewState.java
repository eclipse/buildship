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

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Represents the (persistable) configuration state of the {@link DependenciesView}. Backed by the Eclipse
 * Preferences API.
 */
public final class DependenciesViewState {

    private final String PREF_LINK_TO_SELECTION = "dependenciesview.linkToSelection";

    private boolean linkToSelection;

    public void load() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        this.linkToSelection = prefs.getBoolean(this.PREF_LINK_TO_SELECTION, false);
    }

    public void save() {
        // in Eclipse 3.6 the method InstanceScope.INSTANCE does not exist
        @SuppressWarnings("deprecation")
        IEclipsePreferences prefs = new InstanceScope().getNode(UiPlugin.PLUGIN_ID);
        prefs.putBoolean(this.PREF_LINK_TO_SELECTION, this.linkToSelection);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            UiPlugin.logger().error("Unable to store task view preferences.", e);
        }
    }

    public boolean isLinkToSelection() {
        return this.linkToSelection;
    }

    public void setLinkToSelection(boolean linkToSelection) {
        this.linkToSelection = linkToSelection;
    }

    public void dispose() {
        save();
    }

}
