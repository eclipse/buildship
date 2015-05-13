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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.buildship.core.notification.UserNotification;

/**
 * Implementation of the {@link UserNotification} interface that displays all notifications in a dialog.
 */
public final class DialogUserNotification implements UserNotification {

    @Override
    public void errorOccurred(final String headline, final String message, final String details, final int severity, final Throwable throwable) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                Shell shell = Display.getDefault().getActiveShell();
                ExceptionDetailsDialog dialog = new ExceptionDetailsDialog(shell, headline,  message, details, severity, throwable);
                dialog.open();
            }
        });
    }

}
