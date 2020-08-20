package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.buildship.ui.internal.preferences.GradleCreateWorkspaceCompositePreferencePage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

public class CompositeSelectionDialog extends AbstractCompositeDialog {

	
	private List<IWorkingSet> removedWorkingSets = new ArrayList<IWorkingSet>();
	private List<IWorkingSet> addedWorkingSets = new ArrayList<IWorkingSet>();
	private IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
	
	public CompositeSelectionDialog(Shell parentShell) {
		super(parentShell);
		this.setTitle(WorkspaceCompositeWizardMessages.Title_ConfigureGradleWorkspaceCompositeDialog);
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
		PreferencesUtil.createPropertyDialogOn(this.getShell(), (IWorkingSet) selection.getFirstElement(), 
				"org.eclipse.buildship.ui.GradleCompositePage", null, null).open();
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
		removedWorkingSets.clear();
		super.cancelPressed();
	}

}
