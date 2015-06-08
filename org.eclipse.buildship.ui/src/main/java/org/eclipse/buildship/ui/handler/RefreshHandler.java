package org.eclipse.buildship.ui.handler;

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
import org.eclipse.ui.handlers.HandlerUtil;



public class RefreshHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) currentSelection;
            IAdapterManager adapterManager = Platform.getAdapterManager();
            List<?> list = selection.toList();
            for (Object object : list) {
                IResource adapter = (IResource) adapterManager.getAdapter(object, IResource.class);
                if (adapter != null) {
                    IProject project = adapter.getProject();
                    // TODO Get the Gradle structure from the IProject
                }
            }
        }

        return Status.OK_STATUS;
    }

}
