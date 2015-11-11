package org.eclipse.buildship.ui.util.viewer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public abstract class AbstractGradleViewerFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        IResource resource = Platform.getAdapterManager().getAdapter(element, IResource.class);
        if (resource != null) {
            return isResourceVisible(resource);
        }
        return true;
    }

    protected abstract boolean isResourceVisible(IResource resource);
}
