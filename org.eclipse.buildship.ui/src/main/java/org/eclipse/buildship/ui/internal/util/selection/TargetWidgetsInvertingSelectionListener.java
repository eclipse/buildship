/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
* Listener on a button that keeps the enabling state of its managed target controls in reverse to its selection.
*/
public final class TargetWidgetsInvertingSelectionListener extends SelectionAdapter {

    private final Button source;
    private final ImmutableList<Control> targets;

    public TargetWidgetsInvertingSelectionListener(Button source, Control... targets) {
        this.source = Preconditions.checkNotNull(source);
        this.targets = ImmutableList.copyOf(targets);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        boolean enabled = this.source.getSelection();
        for (Control control : this.targets) {
            control.setEnabled(!enabled);
        }
    }

}
