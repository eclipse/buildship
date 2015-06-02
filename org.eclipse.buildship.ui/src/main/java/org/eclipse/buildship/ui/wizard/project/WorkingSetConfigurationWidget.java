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

package org.eclipse.buildship.ui.wizard.project;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.ImmutableList;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;

/**
 * Enhances the {@link WorkingSetConfigurationBlock} class with {@link WorkingSetChangedListener} functionality.
 *
 * @see WorkingSetChangedListener
 */
public final class WorkingSetConfigurationWidget extends WorkingSetConfigurationBlock {

    private final List<WorkingSetChangedListener> listener;

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

        // add modification listener to the working sets combo
        Combo workingSetsCombo = findWorkingSetsCombo(parent);
        workingSetsCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                fireWorkingSetChanged();
            }
        });
    }

    private Combo findWorkingSetsCombo(Composite parent) {
        Control[] children = parent.getChildren();
        for (Control control : children) {
            if (control instanceof Combo) {
                return (Combo) control;
            } else if (control instanceof Composite) {
                return findWorkingSetsCombo((Composite) control);
            }
        }

        throw new IllegalStateException("Cannot find working sets Combo.");
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

}
