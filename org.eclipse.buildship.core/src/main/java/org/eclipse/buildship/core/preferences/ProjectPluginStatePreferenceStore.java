/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences;

import org.eclipse.core.resources.IProject;

/**
 * Project-scoped preference storage that stores the information in workspace plugin state area.
 * <p/>
 * The functionality is similar to {@code ProjectPreferences}, but here the stored key-value pairs
 * are not stored in the project's {@code .settings} folder, therefore they are not visible by the
 * users.
 * <p/>
 * The service should be accessed via {code CorePlugin#internalProjectPreferenceStore()}.
 *
 * @author Donat Csikos
 *
 */
public interface ProjectPluginStatePreferenceStore {

    /**
     * Reads the project references and returns a snapshot of the current state.
     *
     * @param project the target project
     * @return the preferences
     */
    public ProjectPluginStatePreferences loadProjectPrefs(IProject project);
}
