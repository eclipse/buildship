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

package org.eclipse.buildship.ui.wizard.project;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler which is invoked by the "org.eclipse.buildship.ui.convertproject" command. It uses the
 * {@link GradleProjectConvert} class to start the conversion to a Gradle project.
 *
 */
public class ConvertToGradleProjectHandler extends AbstractHandler {

    private GradleProjectConvert gradleProjectConvert;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        Shell shell = HandlerUtil.getActiveShell(event);
        if (currentSelection instanceof IStructuredSelection) {

            List<?> list = ((IStructuredSelection) currentSelection).toList();
            for (Object element : list) {
                // Get an IResource as an adapter from the current selection
                IAdapterManager adapterManager = Platform.getAdapterManager();
                IResource resource = (IResource) adapterManager.getAdapter(element, IResource.class);

                if (resource != null) {
                    IProject project = resource.getProject();
                    return getProjectConverter().convertProject(project, shell);
                }
            }
        }

        return Status.OK_STATUS;
    }

    protected GradleProjectConvert getProjectConverter() {
        if (null == this.gradleProjectConvert) {
            this.gradleProjectConvert = new GradleProjectConvert();
        }
        return this.gradleProjectConvert;
    }

}
