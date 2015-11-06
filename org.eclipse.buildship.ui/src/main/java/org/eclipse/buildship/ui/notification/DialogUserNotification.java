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

import org.eclipse.buildship.core.notification.UserNotification;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Implementation of the {@link UserNotification} interface that displays all notifications in a dialog.
 */
public final class DialogUserNotification implements UserNotification {

    private volatile ExceptionDetailsDialog dialog;

    @Override
    public void errorOccurred(final String headline, final String message, final String details, final int severity,
            final Throwable... throwables) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                if (DialogUserNotification.this.dialog == null || DialogUserNotification.this.dialog.getShell() == null
                        || DialogUserNotification.this.dialog.getShell().isDisposed()) {
                    DialogUserNotification.this.dialog = new ExceptionDetailsDialog(shell, headline, message, details,
                            severity, throwables);
                    DialogUserNotification.this.dialog.open();
                } else {
                    DialogUserNotification.this.dialog.addException(throwables);
                }
            }
        });
    }

}
