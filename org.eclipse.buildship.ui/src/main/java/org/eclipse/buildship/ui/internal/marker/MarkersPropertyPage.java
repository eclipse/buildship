/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.marker;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

/**
 * Property page displaying details of Gradle problem markers.
 *
 * @author Donat Csikos
 */
public class MarkersPropertyPage extends PropertyPage {


    public MarkersPropertyPage() {
        super();
        noDefaultAndApplyButton();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(5, 20).applyTo(composite);
        GridDataFactory.fillDefaults().applyTo(composite);

        initializeDialogUnits(composite);
        doCreateContents(composite);
        Dialog.applyDialogFont(composite);

        return composite;
    }

    private void doCreateContents(Composite parent) {
        IMarker marker = (IMarker) getElement().getAdapter(IMarker.class);
        String message = marker.getAttribute(IMarker.MESSAGE, "(no message provided)");
        String stacktrace = marker.getAttribute(GradleErrorMarker.ATTRIBUTE_STACKTRACE, "(no stacktrace provided)");

        Label messageLabel = new Label(parent, SWT.NONE);
        messageLabel.setText("Message:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(messageLabel);

        Text messageText = new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        GridDataFactory.fillDefaults().hint(100, convertHeightInCharsToPixels(countLines(message))).align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(messageText);
        messageText.setText(message);

        Label stackTraceLabel = new Label(parent, SWT.NONE);
        stackTraceLabel.setText("Stacktrace:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(stackTraceLabel);

        Text stacktraceAreaText = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().hint(100, 100).grab(true, true).applyTo(stacktraceAreaText);

        stacktraceAreaText.setText(stacktrace);
    }

    public static int countLines(String str) {
        return Lists.newArrayList(Splitter.on(System.lineSeparator()).split(str)).size();
    }
}
