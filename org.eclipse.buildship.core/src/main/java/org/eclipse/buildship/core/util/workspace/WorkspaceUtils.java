/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 472223
 */

package org.eclipse.buildship.core.util.workspace;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * These utility methods can be used to check certain workspace specific
 * settings.
 */
public class WorkspaceUtils {

    public static boolean isInWorkspaceFolder(File file) {
        return isInWorkspaceFolder(new Path(file.getAbsolutePath()));
    }

    public static boolean isInWorkspaceFolder(IPath path) {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        return workspaceLocation.isPrefixOf(path);
    }
}
