package org.eclipse.buildship.ui.internal.workspace;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveCompositeHandler extends AbstractHandler implements IHandler {
	
	private IStructuredSelection initialSelection;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		//HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench().close();
    	IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench().getActiveWorkbenchWindow();
    	
    	if (activeWorkbenchWindow != null) {
			ISelection selection= activeWorkbenchWindow.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection)
				initialSelection = (IStructuredSelection)selection;
    	}
		return null;
	}

}
