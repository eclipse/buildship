/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package com.gradleware.tooling.eclipse.ui.util.selection;

import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Enables {@code Control} instances based on the checked/selected state of a {@code Button}
 * instance.
 */
public final class Enabler {

    private final Button button;
    private final List<Control> controls;
    private final SelectionListener selectionListener;

    public Enabler(Button button) {
        this.button = button;
        this.controls = Lists.newArrayList();
        this.selectionListener = new ButtonSelectionListener();

        this.button.addSelectionListener(this.selectionListener);
    }

    /**
     * Enables the given controls when the enabling button becomes selected, otherwise disables
     * them. When the button is selected, the button also receives the focus.
     *
     * At the time the controls are registered with this enabler, their enabled state is updated
     * based on the current selection state of the button.
     *
     * @param controls the controls to enable
     */
    public void enables(Control... controls) {
        this.controls.addAll(ImmutableList.copyOf(controls));
        updateEnabledStateOfTargetControls();
    }

    private void updateEnabledStateOfTargetControls() {
        boolean enabled = this.button.getSelection();
        for (Control control : this.controls) {
            control.setEnabled(enabled);
            if (enabled) {
                control.setFocus();
            }
        }
    }

    public void dispose() {
        this.button.removeSelectionListener(this.selectionListener);
    }

    /**
     * {@code SelectionListener} that updates the enabled state of the given controls
     * based on the current selection state of the button whose selection changed.
     */
    private final class ButtonSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            updateEnabledStateOfTargetControls();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            updateEnabledStateOfTargetControls();
        }

    }

}
