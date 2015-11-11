package org.eclipse.buildship.ui.util.viewer;

import org.eclipse.core.resources.IResource;

import org.eclipse.buildship.core.util.predicate.Predicates;


public class GradleBuildFolderViewerFilter extends AbstractGradleViewerFilter {

    @Override
    protected boolean isResourceVisible(IResource resource) {
        return Predicates.isBuildFolderVisible().apply(resource);
    }
}
