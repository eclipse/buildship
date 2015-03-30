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

package com.gradleware.tooling.eclipse.ui.util.file;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.gradleware.tooling.eclipse.ui.projectimport.ProjectImportMessages;

/**
 * A {@link org.eclipse.swt.events.SelectionListener} implementation which opens a {@link DirectoryDialog} and puts the
 * path of the selected {@code File} into a target text field.
 */
public final class DirectoryDialogSelectionListener extends SelectionAdapter {

    private final Shell shell;
    private final Text target;
    private final String title;

    public DirectoryDialogSelectionListener(Shell shell, Text target, String entity) {
        this.shell = shell;
        this.target = target;
        this.title = String.format(ProjectImportMessages.Title_Select_0, entity);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        DirectoryDialog directoryDialog = new DirectoryDialog(this.shell, SWT.SHEET);
        directoryDialog.setText(this.title);

        // derive initially selected directory from the text field value
        File startLocation = Strings.isNullOrEmpty(this.target.getText()) ? null : new File(this.target.getText().trim()).getAbsoluteFile();
        if (startLocation != null && startLocation.exists() && startLocation.isFile()) {
            directoryDialog.setFilterPath(startLocation.getParentFile().getAbsolutePath());
        } else if (startLocation != null && startLocation.exists() && startLocation.isDirectory()) {
            directoryDialog.setFilterPath(startLocation.getAbsolutePath());
        } else {
            // set default location to c: on windows and to / on linux/mac
            String currentPlatform = SWT.getPlatform();
            directoryDialog.setFilterPath(currentPlatform.equals("win32") || currentPlatform.equals("wpf") ? "c:\\" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    : "/"); //$NON-NLS-1$
        }

        // show the directory dialog and wait for OK or CANCEL
        // in case of OK put the path of the selected directory on the text field
        String selectedDirectory = directoryDialog.open();
        if (selectedDirectory != null) {
            this.target.setText(selectedDirectory);
        }
    }

}
