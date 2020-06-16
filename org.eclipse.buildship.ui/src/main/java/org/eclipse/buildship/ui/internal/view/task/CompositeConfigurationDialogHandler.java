package org.eclipse.buildship.ui.internal.view.task;

import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeSelectionDialog;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class CompositeConfigurationDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
       	CompositeSelectionDialog dialog = new CompositeSelectionDialog(shell);
       	dialog.open();
		return null;
	}

}
