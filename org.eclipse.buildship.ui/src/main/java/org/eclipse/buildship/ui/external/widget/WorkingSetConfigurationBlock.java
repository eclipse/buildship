/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - bug 201661
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 470477
 *******************************************************************************/
package org.eclipse.buildship.ui.external.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.SimpleWorkingSetSelectionDialog;

import com.google.common.collect.ImmutableList;
import com.ibm.icu.text.Collator;

/**
 * Instances of this class provide a reusable composite with controls that allow
 * the selection of working sets. This class is most useful in
 * {@link org.eclipse.jface.wizard.IWizardPage} instances that wish to create
 * resources and pre-install them into particular working sets.
 *
 * @since 3.4
 *
 */
@SuppressWarnings("restriction")
public class WorkingSetConfigurationBlock {

    /**
     * Filters the given working sets such that the following is true: for each
     * IWorkingSet s in result: s.getId() is element of workingSetIds.
     *
     * @param workingSets
     *            the array to filter
     * @param workingSetIds
     *            the acceptable working set ids
     * @return the filtered elements
     */
    public static IWorkingSet[] filter(IWorkingSet[] workingSets, String[] workingSetIds) {

        // create a copy so we can sort the array without mucking it up for
        // clients.
        String[] workingSetIdsCopy = new String[workingSetIds.length];
        System.arraycopy(workingSetIds, 0, workingSetIdsCopy, 0, workingSetIds.length);
        Arrays.sort(workingSetIdsCopy);

        List<IWorkingSet> result = new ArrayList<IWorkingSet>();

        for (int i = 0; i < workingSets.length; i++) {
            if (Arrays.binarySearch(workingSetIdsCopy, workingSets[i].getId()) >= 0) {
                result.add(workingSets[i]);
            }
        }

        return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
    }

    /**
     * Empty working set array constant.
     */
    private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

    private static final String WORKINGSET_SELECTION_HISTORY = "workingset_selection_history"; //$NON-NLS-1$
    private static final int MAX_HISTORY_SIZE = 5;

    private final List<WorkingSetChangedListener> changeListener = new CopyOnWriteArrayList<WorkingSetChangedListener>();

    private Label workingSetLabel;
    private Combo workingSetCombo;
    private Button selectButton;
    private Button enableButton;

    private IWorkingSet[] selectedWorkingSets;
    private List<String> selectionHistory;
    private final IDialogSettings dialogSettings;
    private final String[] workingSetTypeIds;

    private final String selectLabel;

    private final String comboLabel;

    private final String addButtonLabel;

    /**
     * Create a new instance of this working set block using default labels.
     *
     * @param workingSetIds
     *            working set ids from which the user can choose
     * @param settings
     *            to store/load the selection history
     */
    public WorkingSetConfigurationBlock(String[] workingSetIds, IDialogSettings settings) {
        this(settings, workingSetIds);
    }
    
    /**
     * Create a new instance of this working set block using default labels.
     *
     * @param workingSetIds
     *            working set ids from which the user can choose
     * @param settings
     *            to store/load the selection history
     */
    public WorkingSetConfigurationBlock(IDialogSettings settings, String... workingSetIds) {
    	this(workingSetIds, settings, null, null, null);
    }

    /**
     * Create a new instance of this working set block using custom labels.
     *
     * @param workingSetIds
     *            working set ids from which the user can choose
     * @param settings
     *            to store/load the selection history
     * @param addButtonLabel
     *            the label to use for the checkable enablement button. May be
     *            <code>null</code> to use the default value.
     * @param comboLabel
     *            the label to use for the recent working set combo. May be
     *            <code>null</code> to use the default value.
     * @param selectLabel
     *            the label to use for the select button. May be
     *            <code>null</code> to use the default value.
     */
    public WorkingSetConfigurationBlock(String[] workingSetIds, IDialogSettings settings, String addButtonLabel,
            String comboLabel, String selectLabel) {
        Assert.isNotNull(workingSetIds);
        Assert.isNotNull(settings);

        this.workingSetTypeIds = workingSetIds;
        Arrays.sort(workingSetIds); // we'll be performing some searches with
                                    // these later - presort them
        this.selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
        this.dialogSettings = settings;
        this.selectionHistory = loadSelectionHistory(settings, workingSetIds);

        this.addButtonLabel = addButtonLabel == null ? WorkbenchMessages.WorkingSetGroup_EnableWorkingSet_button
                : addButtonLabel;
        this.comboLabel = comboLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_WorkingSetText_name
                : comboLabel;
        this.selectLabel = selectLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_SelectWorkingSet_button
                : selectLabel;
    }

    /**
     * Set the current selection in the workbench.
     *
     * @param selection
     *            the selection to present in the UI or <b>null</b>
     * @deprecated use {@link #setWorkingSets(IWorkingSet[])} and
     *             {@link #findApplicableWorkingSets(IStructuredSelection)}
     *             instead.
     */
    @Deprecated
    public void setSelection(IStructuredSelection selection) {
        this.selectedWorkingSets = findApplicableWorkingSets(selection);

        if (this.workingSetCombo != null) {
            updateSelectedWorkingSets();
        }
    }

    /**
     * Set the current selection of working sets. This array will be filtered to
     * contain only working sets that are applicable to this instance.
     *
     * @param workingSets
     *            the working sets
     */
    public void setWorkingSets(IWorkingSet[] workingSets) {
        this.selectedWorkingSets = filterWorkingSets(Arrays.asList(workingSets));
        if (this.workingSetCombo != null) {
            updateSelectedWorkingSets();
        }
    }

    /**
     * Retrieves a working set from the given <code>selection</code> or an empty
     * array if no working set could be retrieved. This selection is filtered
     * based on the criteria used to construct this instance.
     *
     * @param selection
     *            the selection to retrieve the working set from
     * @return the selected working set or an empty array
     */
    public IWorkingSet[] findApplicableWorkingSets(IStructuredSelection selection) {
        if (selection == null) {
            return EMPTY_WORKING_SET_ARRAY;
        }

        return filterWorkingSets(selection.toList());
    }

    public void addWorkingSetChangeListener(WorkingSetChangedListener workingSetListener) {
        this.changeListener.add(workingSetListener);
    }

    public void removeWorkingSetChangeListener(WorkingSetChangedListener workingSetListener) {
        this.changeListener.remove(workingSetListener);
    }

    private void fireWorkingSetChanged() {
        ImmutableList<IWorkingSet> workingSets = ImmutableList.copyOf(getSelectedWorkingSets());
        for (WorkingSetChangedListener workingSetChangedListener : this.changeListener) {
            workingSetChangedListener.workingSetsChanged(workingSets);
        }
    }

    public void addWorkingSetEnabledListener(Listener workingSetListener) {
        this.enableButton.addListener(SWT.Selection, workingSetListener);
    }

    public void removeWorkingSetEnabledListener(Listener workingSetListener) {
        this.enableButton.removeListener(SWT.Selection, workingSetListener);
    }

    /**
     * Prune a list of working sets such that they all match the criteria set
     * out by this block.
     *
     * @param elements
     *            the elements to filter
     * @return the filtered elements
     */
    private IWorkingSet[] filterWorkingSets(Collection<?> elements) {
        List<IWorkingSet> result = new ArrayList<IWorkingSet>();
        for (Object element : elements) {
            if (element instanceof IWorkingSet && verifyWorkingSet((IWorkingSet) element)) {
                result.add((IWorkingSet) element);
            }
        }
        return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
    }

    /**
     * Verifies that the given working set is suitable for selection in this
     * block.
     *
     * @param workingSetCandidate
     *            the candidate to test
     * @return whether it is suitable
     */
    private boolean verifyWorkingSet(IWorkingSet workingSetCandidate) {
        return !workingSetCandidate.isAggregateWorkingSet()
                && Arrays.binarySearch(this.workingSetTypeIds, workingSetCandidate.getId()) >= 0;
    }

    /**
     * Return the currently selected working sets. If the controls representing
     * this block are disabled this array will be empty regardless of the
     * currently selected values.
     *
     * @return the selected working sets
     */
    public IWorkingSet[] getSelectedWorkingSets() {
        if (this.enableButton.getSelection()) {
            return this.selectedWorkingSets;
        }
        return EMPTY_WORKING_SET_ARRAY;
    }

    /**
     * Add this block to the <code>parent</code>.
     *
     * @param parent
     *            the parent to add the block to
     */
    public void createContent(final Composite parent) {
        int numColumn = 3;

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setLayout(new GridLayout(numColumn, false));

        this.enableButton = new Button(composite, SWT.CHECK);
        this.enableButton.setText(this.addButtonLabel);
        GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        enableData.horizontalSpan = numColumn;
        this.enableButton.setLayoutData(enableData);
        this.enableButton.setSelection(this.selectedWorkingSets.length > 0);

        this.workingSetLabel = new Label(composite, SWT.NONE);
        this.workingSetLabel.setText(this.comboLabel);

        this.workingSetCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
        GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textData.horizontalSpan = numColumn - 2;
        textData.horizontalIndent = 0;
        this.workingSetCombo.setLayoutData(textData);

        this.selectButton = new Button(composite, SWT.PUSH);
        this.selectButton.setText(this.selectLabel);
        setButtonLayoutData(this.selectButton);
        this.selectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SimpleWorkingSetSelectionDialog dialog = new SimpleWorkingSetSelectionDialog(parent.getShell(),
                        WorkingSetConfigurationBlock.this.workingSetTypeIds,
                        WorkingSetConfigurationBlock.this.selectedWorkingSets, false);
                dialog.setMessage(WorkbenchMessages.WorkingSetGroup_WorkingSetSelection_message);

                if (dialog.open() == Window.OK) {
                    IWorkingSet[] result = dialog.getSelection();
                    if (result != null && result.length > 0) {
                        WorkingSetConfigurationBlock.this.selectedWorkingSets = result;
                        PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(result[0]);
                    } else {
                        WorkingSetConfigurationBlock.this.selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
                    }
                    updateWorkingSetSelection();
                }
            }
        });

        this.enableButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnableState(WorkingSetConfigurationBlock.this.enableButton.getSelection());
                fireWorkingSetChanged();
            }
        });
        updateEnableState(this.enableButton.getSelection());

        this.workingSetCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedWorkingSets();
                fireWorkingSetChanged();
            }
        });

        this.workingSetCombo.setItems(getHistoryEntries());
        if (this.selectedWorkingSets.length == 0 && this.selectionHistory.size() > 0) {
            this.workingSetCombo.select(historyIndex((String) this.selectionHistory.get(0)));
            updateSelectedWorkingSets();
        } else {
            updateWorkingSetSelection();
        }
    }

    public void modifyCurrentWorkingSetItem(IWorkingSet[] result) {
        this.selectedWorkingSets = result;
        if (result.length > 0) {
            PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(result[0]);
        }

        updateWorkingSetSelection();
    }

    public void setWorkingSetActive(boolean activate) {
        this.enableButton.setSelection(activate);
        updateEnableState(activate);
    }

    public boolean isWorkingSetEnabled() {
        return this.enableButton.getSelection();
    }

    private void updateEnableState(boolean enabled) {
        this.workingSetLabel.setEnabled(enabled);
        this.workingSetCombo
                .setEnabled(enabled && (this.selectedWorkingSets.length > 0 || getHistoryEntries().length > 0));
        this.selectButton.setEnabled(enabled);
    }

    private void updateWorkingSetSelection() {
        if (this.selectedWorkingSets.length > 0) {
            this.workingSetCombo.setEnabled(true);
            StringBuffer buf = new StringBuffer();

            buf.append(this.selectedWorkingSets[0].getLabel());
            for (int i = 1; i < this.selectedWorkingSets.length; i++) {
                IWorkingSet ws = this.selectedWorkingSets[i];
                buf.append(',').append(' ');
                buf.append(ws.getLabel());
            }

            String currentSelection = buf.toString();
            int index = historyIndex(currentSelection);
            historyInsert(currentSelection);
            if (index >= 0) {
                this.workingSetCombo.select(index);
            } else {
                this.workingSetCombo.setItems(getHistoryEntries());
                this.workingSetCombo.select(historyIndex(currentSelection));
            }
        } else {
            this.enableButton.setSelection(false);
            updateEnableState(false);
        }
    }

    private String[] getHistoryEntries() {
        String[] history = (String[]) this.selectionHistory.toArray(new String[this.selectionHistory.size()]);
        Arrays.sort(history, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Collator.getInstance().compare(o1, o2);
            }
        });
        return history;
    }

    private void historyInsert(String entry) {
        this.selectionHistory.remove(entry);
        this.selectionHistory.add(0, entry);
        storeSelectionHistory(this.dialogSettings);
    }

    private int historyIndex(String entry) {
        for (int i = 0; i < this.workingSetCombo.getItemCount(); i++) {
            if (this.workingSetCombo.getItem(i).equals(entry)) {
                return i;
            }
        }

        return -1;
    }

    // copied from org.eclipse.jdt.internal.ui.text.JavaCommentScanner
    private String[] split(String value, String delimiters) {
        StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
        int size = tokenizer.countTokens();
        String[] tokens = new String[size];
        int i = 0;
        while (i < size) {
            tokens[i++] = tokenizer.nextToken();
        }
        return tokens;
    }

    private void updateSelectedWorkingSets() {
        String item = this.workingSetCombo.getItem(this.workingSetCombo.getSelectionIndex());
        String[] workingSetNames = split(item, ", "); //$NON-NLS-1$

        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        this.selectedWorkingSets = new IWorkingSet[workingSetNames.length];
        for (int i = 0; i < workingSetNames.length; i++) {
            IWorkingSet set = workingSetManager.getWorkingSet(workingSetNames[i]);
            Assert.isNotNull(set);
            this.selectedWorkingSets[i] = set;
        }
    }

    private void storeSelectionHistory(IDialogSettings settings) {
        String[] history;
        if (this.selectionHistory.size() > MAX_HISTORY_SIZE) {
            List<String> subList = this.selectionHistory.subList(0, MAX_HISTORY_SIZE);
            history = (String[]) subList.toArray(new String[subList.size()]);
        } else {
            history = (String[]) this.selectionHistory.toArray(new String[this.selectionHistory.size()]);
        }
        settings.put(WORKINGSET_SELECTION_HISTORY, history);
    }

    private List<String> loadSelectionHistory(IDialogSettings settings, String[] workingSetIds) {
        String[] strings = settings.getArray(WORKINGSET_SELECTION_HISTORY);
        if (strings == null || strings.length == 0) {
            return new ArrayList<String>();
        }

        List<String> result = new ArrayList<String>();

        Set<String> workingSetIdsSet = new HashSet<String>(Arrays.asList(workingSetIds));

        IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        for (int i = 0; i < strings.length; i++) {
            String[] workingSetNames = split(strings[i], ", "); //$NON-NLS-1$
            boolean valid = true;
            for (int j = 0; j < workingSetNames.length && valid; j++) {
                IWorkingSet workingSet = workingSetManager.getWorkingSet(workingSetNames[j]);
                if (workingSet == null) {
                    valid = false;
                } else {
                    if (!workingSetIdsSet.contains(workingSet.getId())) {
                        valid = false;
                    }
                }
            }
            if (valid) {
                result.add(strings[i]);
            }
        }

        return result;
    }

    /*
     * Copy from DialogPage with changes to accomodate the lack of a Dialog
     * context.
     */
    private GridData setButtonLayoutData(Button button) {
        button.setFont(JFaceResources.getDialogFont());

        GC gc = new GC(button);
        gc.setFont(button.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return data;
    }
}