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

package org.eclipse.buildship.ui.notification;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.notification.UserNotification;

/**
 * Implementation of the {@link UserNotification} interface that displays all notifications in a
 * dialog.
 */
public final class DialogUserNotification implements UserNotification {

    private ExceptionDetailsDialog dialog;

    @Override
    public void errorOccurred(final String headline, final String message, final String details, final int severity, final Throwable throwable) {
        // since the dialog is always accessed from the UI thread there is no need for
        // further synchronization
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                if (noDialogVisible()) {
                    createAndOpenDialog(shell, headline, message, details, severity, throwable);
                } else {
                    addExceptionToDialog(throwable);
                }
            }
        });
    }

    private boolean noDialogVisible() {
        return dialog == null || dialog.getShell() == null || dialog.getShell().isDisposed();
    }

    private void createAndOpenDialog(Shell shell, final String title, final String message, final String details, final int severity, final Throwable throwable) {
        dialog = new ExceptionDetailsDialog(shell, title, message, details, severity, throwable);
        dialog.setBlockOnOpen(false);
        dialog.open();
    }

    private void addExceptionToDialog(Throwable throwable) {
        this.dialog.addException(throwable);
    }

}
