package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.buildship.ui.internal.util.layout.LayoutUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class AbstractCompositeDialog extends SelectionDialog {
	
	private Button 	newButton;
	private Button 	editButton;
	private Button 	removeButton;
	private TableViewer compositeSelectionListViewer;
	List<IWorkingSet> composites = new ArrayList<IWorkingSet>();
	private ColumnLabelProvider labelProvider = new ColumnLabelProvider() {
		private Map<ImageDescriptor, Image> icons = new Hashtable<>();
		@Override
		public String getText(Object element) {
			Assert.isTrue(element instanceof IWorkingSet);
			return ((IWorkingSet) element).getName();
		}
		
		@Override
		public Image getImage(Object element) {
			Assert.isTrue(element instanceof IWorkingSet);
			ImageDescriptor imgDescriptor= ((IWorkingSet) element).getImageDescriptor();
			if (imgDescriptor == null)
				return null;
			Image icon= icons.get(imgDescriptor);
			if (icon == null) {
				icon= imgDescriptor.createImage();
				icons.put(imgDescriptor, icon);
			}
			return icon;
		}
	};
	private IStructuredContentProvider contentProvider;
	
	protected AbstractCompositeDialog(Shell parentShell) {
		super(parentShell);
		contentProvider = new ArrayContentProvider();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = createComponents(parent);
		addListeners();
		loadCompositeNames();
        return container;
	}

	private void loadCompositeNames() {
		compositeSelectionListViewer.setLabelProvider(labelProvider);
		compositeSelectionListViewer.setContentProvider(contentProvider);
		IWorkingSet[] workingSetArray = PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets();
		for (IWorkingSet workingSet : workingSetArray) {
			if (!workingSet.isAggregateWorkingSet() && workingSet.getId().equals(IGradleCompositeIDs.NATURE)) {
				composites.add(workingSet);
			}
		}
		compositeSelectionListViewer.setInput(composites);
		compositeSelectionListViewer.addSelectionChangedListener(e -> selectionChanged());
	}

	private Object selectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) compositeSelectionListViewer.getSelection();
		this.removeButton.setEnabled(!selection.isEmpty());
		this.editButton.setEnabled(selection.size() == 1);
		return null;
	}

	private Composite createComponents(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(LayoutUtils.newGridLayout(3));
		createCompositeCheckboxList(container);
		createBottomButtonContainer(container);
		return container;
	}

	private void createBottomButtonContainer(Composite container) {
		Composite buttonContainer = new Composite(container, SWT.NONE);
		buttonContainer.setLayout(LayoutUtils.newGridLayout(3));
		buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		this.newButton = new Button(buttonContainer, SWT.PUSH);
        this.newButton.setText(WorkspaceCompositeWizardMessages.CompositeConfigurationDialog_New);
        this.newButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        this.editButton = new Button(buttonContainer, SWT.PUSH);
        this.editButton.setText(WorkspaceCompositeWizardMessages.CompositeConfigurationDialog_Edit);
        this.editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.editButton.setEnabled(false);
        
        this.removeButton = new Button(buttonContainer, SWT.PUSH);
        this.removeButton.setText(WorkspaceCompositeWizardMessages.CompositeConfigurationDialog_Remove);
        this.removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.removeButton.setEnabled(false);
	}

	private void createCompositeCheckboxList(Composite container) {
		this.compositeSelectionListViewer = new TableViewer(new Table(container, SWT.BORDER));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 250;
		data.horizontalSpan = 2;
		this.compositeSelectionListViewer.getTable().setLayoutData(data);
	}
	
	private void addListeners() {
		this.newButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
                createNewWorkspaceComposite();
            }
		});
		this.editButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
                editWorkspaceComposite();
            }
		});
		this.removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
                removeWorkspaceComposite();
            }
		});
	}
	
	protected ISelection getSelectedComposites() {
		return this.compositeSelectionListViewer.getSelection();
	}
	
	protected void removeSelectedCompositesFromList(IStructuredSelection selection) {
		List<IWorkingSet> removedComposites = selection.toList();
		composites.removeAll(removedComposites);
		this.compositeSelectionListViewer.refresh();
	}
	
	protected void addNewCreatedComposite(IWorkingSet workingSet) {
		composites.add(workingSet);
		this.compositeSelectionListViewer.add(workingSet);
	}

	protected abstract void createNewWorkspaceComposite();
	protected abstract void editWorkspaceComposite();
	protected abstract void removeWorkspaceComposite();

}
