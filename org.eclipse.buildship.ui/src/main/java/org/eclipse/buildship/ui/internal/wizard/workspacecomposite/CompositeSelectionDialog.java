package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class CompositeSelectionDialog extends AbstractCompositeDialog {
	
	
	public CompositeSelectionDialog(Shell parentShell) {
		super(parentShell);
		this.setTitle(WorkspaceCompositeWizardMessages.Title_ConfigureGradleWorkspaceCompositeDialog);
	}

	@Override
	protected void createNewWorkspaceComposite() {
		// TODO (kuzniarz) Open composite creation wizard
		WizardDialog dialog = new WizardDialog(this.getShell(), new WorkspaceCompositeCreationWizard());
		dialog.open();
	}

	@Override
	protected void editWorkspaceComposite() {
		// TODO (kuzniarz) Composite properties for selected composite should be opened
		
	}

	@Override
	protected void removeWorkspaceComposite() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed() {
		// TODO Auto-generated method stub
		super.cancelPressed();
	}

}
