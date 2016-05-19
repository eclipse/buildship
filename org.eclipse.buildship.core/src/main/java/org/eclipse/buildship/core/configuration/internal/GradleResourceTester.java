package org.eclipse.buildship.core.configuration.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.buildship.core.configuration.GradleProjectNature;

public class GradleResourceTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IResource) {
            IResource resource = (IResource) receiver;
            IProject project = resource.getProject();
            if (project != null && GradleProjectNature.isPresentOn(project)) {
                return "gradle".equals(resource.getFileExtension()) || "gradle.properties".equals(resource.getName());
            }
        }
        return false;
    }

}
