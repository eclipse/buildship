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

package org.eclipse.buildship.ui.util.file;

import java.io.File;

import com.google.common.base.Strings;

import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.buildship.ui.wizard.project.ProjectWizardMessages;

/**
 * A {@link org.eclipse.swt.events.SelectionListener} implementation which opens a
 * {@link DirectoryDialog} and puts the path of the selected {@code File} into a target widget. All
 * suitable widgets for {@link WidgetProperties#text()} are valid.
 */
public final class DirectoryDialogSelectionListener extends SelectionAdapter {

    private final Shell shell;
    private final String title;
    private ISWTObservableValue target;

    public DirectoryDialogSelectionListener(Shell shell, Widget target, String entity) {
        this.shell = shell;
        this.target = WidgetProperties.text().observe(target);
        this.title = NLS.bind(ProjectWizardMessages.Title_Select_0, entity);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        DirectoryDialog directoryDialog = new DirectoryDialog(this.shell, SWT.SHEET);
        directoryDialog.setText(this.title);

        // derive initially selected directory from the text field value
        String text = String.valueOf(target.getValue());
        File startLocation = Strings.isNullOrEmpty(text) ? null : new File(text.trim()).getAbsoluteFile();
        if (startLocation != null && startLocation.exists() && startLocation.isFile()) {
            directoryDialog.setFilterPath(startLocation.getParentFile().getAbsolutePath());
        } else if (startLocation != null && startLocation.exists() && startLocation.isDirectory()) {
            directoryDialog.setFilterPath(startLocation.getAbsolutePath());
        } else {
            String userHomeDir = System.getProperty("user.home");  //$NON-NLS-1$
            directoryDialog.setFilterPath(userHomeDir);
        }

        // show the directory dialog and wait for OK or CANCEL
        // in case of OK put the path of the selected directory on the text field
        String selectedDirectory = directoryDialog.open();
        if (selectedDirectory != null) {
            target.setValue(selectedDirectory);
        }
    }

}
