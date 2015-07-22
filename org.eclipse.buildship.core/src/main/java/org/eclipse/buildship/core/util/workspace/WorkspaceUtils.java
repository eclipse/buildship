package org.eclipse.buildship.core.util.workspace;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class WorkspaceUtils {

    public static boolean isInWorkspaceFolder(File file) {
        return isInWorkspaceFolder(new Path(file.getAbsolutePath()));
    }

    public static boolean isInWorkspaceFolder(IPath path) {
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        return workspaceLocation.isPrefixOf(path);
    }
}
