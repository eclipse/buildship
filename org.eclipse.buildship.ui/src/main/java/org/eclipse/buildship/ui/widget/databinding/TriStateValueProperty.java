/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.widget.databinding;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;

import org.eclipse.buildship.ui.widget.TriState;

/**
 * WidgetValueProperty, which can be used to observe a {@link TriState}.
 *
 */
public class TriStateValueProperty extends WidgetValueProperty {

    public TriStateValueProperty() {
        super(SWT.Selection);
    }

    @Override
    public Object getValueType() {
        return null;
    }

    @Override
    protected Object doGetValue(Object source) {
        if (source instanceof TriState) {
            return ((TriState) source).getState();
        }

        return null;
    }

    @Override
    protected void doSetValue(Object source, Object value) {
        if (source instanceof TriState) {
            ((TriState) source).setState((Boolean) value);
        }
    }

}
