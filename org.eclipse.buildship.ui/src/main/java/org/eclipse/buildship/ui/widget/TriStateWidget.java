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

package org.eclipse.buildship.ui.widget;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This widget is used to make it possible to have a tri state, which means <code>true</code>,
 * <code>false</code> and <code>null</code> for undefined.
 *
 */
public class TriStateWidget extends Composite implements TriState {

    private ListenerList selectionListener = new ListenerList();

    private Button trueButton;
    private Button falseButton;
    private Button undefinedButton;

    public TriStateWidget(Composite parent, int style) {
        super(parent, style);
        setLayout(new RowLayout());

        trueButton = new Button(this, SWT.RADIO);
        trueButton.setText("true");
        addListener(trueButton);

        falseButton = new Button(this, SWT.RADIO);
        falseButton.setText("false");
        addListener(falseButton);

        undefinedButton = new Button(this, SWT.RADIO);
        undefinedButton.setText("undefined");
        addListener(undefinedButton);
    }

    private void addListener(Button button) {
        button.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Object[] listeners = selectionListener.getListeners();
                for (Object object : listeners) {
                    if (object instanceof Listener) {
                        ((Listener) object).handleEvent(event);
                    }
                }
            }
        });
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        if (SWT.Selection == eventType) {
            selectionListener.add(listener);
            return;
        }

        super.addListener(eventType, listener);
    }

    @Override
    public void removeListener(int eventType, Listener listener) {
        if (SWT.Selection == eventType) {
            selectionListener.remove(listener);
        }

        super.removeListener(eventType, listener);
    }

    @Override
    public Boolean getState() {
        if (trueButton.getSelection()) {
            return Boolean.TRUE;
        } else if (falseButton.getSelection()) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public void setState(Boolean state) {
        if (null == state) {
            undefinedButton.setSelection(true);
            trueButton.setSelection(false);
            falseButton.setSelection(false);
        } else if (state) {
            undefinedButton.setSelection(false);
            trueButton.setSelection(true);
            falseButton.setSelection(false);
        } else {
            undefinedButton.setSelection(false);
            trueButton.setSelection(false);
            falseButton.setSelection(true);
        }
    }
}
