package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;

public class CompositeSelectionDialog extends AbstractCompositeDialog {

	
	private List<IWorkingSet> removedWorkingSets = new ArrayList<IWorkingSet>();
	private List<IWorkingSet> addedWorkingSets = new ArrayList<IWorkingSet>();
	private List<IWorkingSet> editedWorkingSets = new ArrayList<IWorkingSet>();
	private IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
	private IWorkingSet[] result;
	
	public CompositeSelectionDialog(Shell parentShell) {
		super(parentShell);
		this.setTitle(WorkspaceCompositeWizardMessages.Title_ConfigureGradleWorkspaceCompositeDialog);
		IWorkingSet[] workingSets = getActiveWorkingSets();
		setSelection(workingSets);
		result = workingSets;
	}

	private IWorkingSet[] getActiveWorkingSets() {
		return new IWorkingSet[0];
	}

	@Override
	protected void createNewWorkspaceComposite() {
		WorkspaceCompositeCreationWizard wizard = new WorkspaceCompositeCreationWizard();
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		dialog.create();
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet= wizard.getComposite();
			addNewCreatedComposite(workingSet);
			manager.addWorkingSet(workingSet);
			addedWorkingSets.add(workingSet);
		}
	}

	@Override
	protected void editWorkspaceComposite() {
		IStructuredSelection selection = (IStructuredSelection) getSelectedComposites();
		IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard((IWorkingSet) selection.getFirstElement());
		WizardDialog dialog = new WizardDialog(this.getShell(), wizard);
		dialog.create();
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet= wizard.getSelection();
			editedWorkingSets.add(workingSet);
		}
	}

	@Override
	protected void removeWorkspaceComposite() {
		IStructuredSelection selectedELements = (IStructuredSelection) getSelectedComposites();
		removedWorkingSets.addAll(selectedELements.toList());
		removeSelectedCompositesFromList(selectedELements);
	}
	
	@Override
	protected void okPressed() {
		if (!removedWorkingSets.isEmpty()) {
			for (Iterator<IWorkingSet> it = removedWorkingSets.iterator(); it.hasNext();) {
				IWorkingSet workingSet = it.next();
				manager.removeWorkingSet(workingSet);
				it.remove();
			}
		}
		addedWorkingSets.clear();
		editedWorkingSets.clear();
		//TODO (kuzniarz) Enabling/Disabling composites in the workspace
		System.out.println(result);
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed() {
		if (!addedWorkingSets.isEmpty()) {
			for (Iterator<IWorkingSet> it = addedWorkingSets.iterator(); it.hasNext();) {
				IWorkingSet workingSet = it.next();
				manager.removeWorkingSet(workingSet);
				it.remove();
			}
		}
		//restore edited composites
		// TODO Auto-generated method stub
		super.cancelPressed();
	}

}
