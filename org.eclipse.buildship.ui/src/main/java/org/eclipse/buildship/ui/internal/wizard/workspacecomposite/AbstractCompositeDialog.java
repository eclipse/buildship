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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

public abstract class AbstractCompositeDialog extends SelectionDialog {
	
	private Button 	newButton;
	private Button 	editButton;
	private Button 	removeButton;
	private Button 	selectAllButton;
	private Button 	deselectAllButton;
	private CheckboxTableViewer compositeSelectionListViewer;
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
	
	protected AbstractCompositeDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = createComponents(parent);
		addListeners();
		loadCompositeNames();
		//initial composite selection
        return container;
	}

	private void loadCompositeNames() {
		compositeSelectionListViewer.setLabelProvider(labelProvider);
		compositeSelectionListViewer.setContentProvider(ArrayContentProvider.getInstance());
		IWorkingSet[] workingSetArray = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
		List<IWorkingSet> composites = new ArrayList<IWorkingSet>();
		for (IWorkingSet workingSet : workingSetArray) {
			if (workingSet.getId().equals(IGradleCompositeIDs.NATURE)) {
				composites.add(workingSet);
			}
		}
		compositeSelectionListViewer.setInput(composites);	
	}

	private Composite createComponents(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(LayoutUtils.newGridLayout(4));
		createCompositeCheckboxList(container);
		createSideButtonContainer(container);
		createBottomButtonContainer(container);
		return container;
	}

	private void createBottomButtonContainer(Composite container) {
		Composite buttonContainer = new Composite(container, SWT.NONE);
		buttonContainer.setLayout(LayoutUtils.newGridLayout(3));
		buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		this.newButton = new Button(buttonContainer, SWT.PUSH);
        this.newButton.setText("New..."); //TODO (kuzniarz) Replace String with constant
        this.newButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        this.editButton = new Button(buttonContainer, SWT.PUSH);
        this.editButton.setText("Edit...");
        this.editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        this.removeButton = new Button(buttonContainer, SWT.PUSH);
        this.removeButton.setText("Remove");
        this.removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	private void createSideButtonContainer(Composite container) {
		Composite sideButtonContainer = new Composite(container, SWT.NONE);
		sideButtonContainer.setLayout(LayoutUtils.newGridLayout(1));
		sideButtonContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		this.selectAllButton = new Button(sideButtonContainer, SWT.PUSH);
        this.selectAllButton.setText("Select All");
        this.selectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        this.deselectAllButton = new Button(sideButtonContainer, SWT.PUSH);
        this.deselectAllButton.setText("Deselect All");
        this.deselectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	private void createCompositeCheckboxList(Composite container) {
		this.compositeSelectionListViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.MULTI);
		this.compositeSelectionListViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
	}
	
	private void addListeners() {
		// TODO Auto-generated method stub
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
		this.selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				selectAllWorkspaceComposites();
            }
		});
		this.deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				deselectAllWorkspaceComposites();
            }
		});
	}

	protected void deselectAllWorkspaceComposites() {
		compositeSelectionListViewer.setCheckedElements(new Object[0]);
	}

	protected void selectAllWorkspaceComposites() {
		compositeSelectionListViewer.setCheckedElements(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets());
	}
	
	protected abstract void createNewWorkspaceComposite();
	protected abstract void editWorkspaceComposite();
	protected abstract void removeWorkspaceComposite();

}
