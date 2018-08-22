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

package org.eclipse.buildship.ui.internal.wizard.project;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;

/**
 * Enhances the {@link WorkingSetConfigurationBlock} class with {@link WorkingSetChangedListener}
 * functionality.
 *
 * @see WorkingSetChangedListener
 */
public final class WorkingSetConfigurationWidget extends WorkingSetConfigurationBlock {

    private final List<WorkingSetChangedListener> listener;

    private Button workingSetsEnabledButton;
    private Combo workingSetsCombo;
    private Button workingSetsSelectButton;

    public WorkingSetConfigurationWidget(String[] workingSetIds, IDialogSettings settings) {
        super(workingSetIds, settings);
        this.listener = new CopyOnWriteArrayList<WorkingSetChangedListener>();
    }

    @SuppressWarnings("UnusedDeclaration")
    public WorkingSetConfigurationWidget(String[] workingSetIds, IDialogSettings settings, String addButtonLabel, String comboLabel, String selectLabel) {
        super(workingSetIds, settings, addButtonLabel, comboLabel, selectLabel);
        this.listener = new CopyOnWriteArrayList<WorkingSetChangedListener>();
    }

    @Override
    public void createContent(Composite parent) {
        super.createContent(parent);

        // remove the colon from the 'Working sets:' label
        Label workingSetsLabel = findWorkingSetsLabel(parent);
        workingSetsLabel.setText(workingSetsLabel.getText().replace(":", ""));

        // add modification listener to the working sets checkbox
        this.workingSetsEnabledButton = findWorkingSetsEnabledButton(parent);
        this.workingSetsEnabledButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fireWorkingSetChanged();
            }
        });

        // add modification and selection change listener to the working sets combo
        this.workingSetsCombo = findWorkingSetsCombo(parent);
        this.workingSetsCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                fireWorkingSetChanged();
            }
        });

        this.workingSetsCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fireWorkingSetChanged();
            }
        });

        // fish out the Select button
        this.workingSetsSelectButton = findWorkingSetsSelectButton(parent);
    }

    private Label findWorkingSetsLabel(Composite parent) {
        return (Label) findControl(parent, Predicates.instanceOf(Label.class));
    }

    private Button findWorkingSetsEnabledButton(Composite parent) {
        return (Button) findControl(parent, Predicates.instanceOf(Button.class));
    }

    private Combo findWorkingSetsCombo(Composite parent) {
        return (Combo) findControl(parent, Predicates.instanceOf(Combo.class));
    }

    private Button findWorkingSetsSelectButton(Composite parent) {
        Predicate<Object> isButton = Predicates.instanceOf(Button.class);
        Predicate<Control> hasPushStyle = new Predicate<Control>() {

            @Override
            public boolean apply(Control control) {
                return (control.getStyle() & SWT.PUSH) == SWT.PUSH;
            }
        };
        return (Button) findControl(parent, Predicates.and(isButton, hasPushStyle));
    }

    private Control findControl(Composite parent, Predicate<? super Control> predicate) {
        Control result = findControlRecursively(parent, predicate);
        if (result != null) {
            return result;
        } else {
            throw new IllegalStateException("Cannot find control under the root composite matching to the provided condition.");
        }
    }

    private Control findControlRecursively(Composite parent, Predicate<? super Control> predicate) {
        Control[] children = parent.getChildren();
        for (Control control : children) {
            if (predicate.apply(control)) {
                return control;
            } else if (control instanceof Composite) {
                Control result = findControlRecursively((Composite) control, predicate);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public Button getWorkingSetsEnabledButton() {
        return this.workingSetsEnabledButton;
    }

    public Combo getWorkingSetsCombo() {
        return this.workingSetsCombo;
    }

    public Button getWorkingSetsSelectButton() {
        return this.workingSetsSelectButton;
    }

    public void addWorkingSetChangeListener(WorkingSetChangedListener workingSetListener) {
        this.listener.add(workingSetListener);
    }

    public void removeWorkingSetChangeListener(WorkingSetChangedListener workingSetListener) {
        this.listener.remove(workingSetListener);
    }

    private void fireWorkingSetChanged() {
        ImmutableList<IWorkingSet> workingSets = ImmutableList.copyOf(getSelectedWorkingSets());
        for (WorkingSetChangedListener workingSetChangedListener : this.listener) {
            workingSetChangedListener.workingSetsChanged(workingSets);
        }
    }

    public void modifyCurrentWorkingSetItem(IWorkingSet[] result) {
        // this method changes the text of the currently selected label such that the target working sets are visible
        // this is the exact behavior what happens when the working set is changed with the dialog box
        // but because that implementation is private we can call it only via reflection
        try {
            Field selectedWorkingSets = WorkingSetConfigurationBlock.class.getDeclaredField("selectedWorkingSets");
            selectedWorkingSets.setAccessible(true);

            selectedWorkingSets.set(this, result);
            if (result.length > 0) {
                PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(result[0]);
            }

            Method updateWorkingSetSelection = WorkingSetConfigurationBlock.class.getDeclaredMethod("updateWorkingSetSelection");
            updateWorkingSetSelection.setAccessible(true);
            updateWorkingSetSelection.invoke(this);
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}
